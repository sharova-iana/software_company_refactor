package org.informatics.service.impl;

import org.informatics.data.Company;
import org.informatics.data.Employee;
import org.informatics.data.Team;
import org.informatics.data.enums.Position;
import org.informatics.exceptions.TeamAssignmentException;
import org.informatics.service.TeamService;
import org.informatics.service.util.DomainLookupService;

import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides the concrete business logic implementation for organizational team building and member coordination workflows.
 * This implementation manages structural company state mutations and records tracking audit logs to the log stream.
 */
public class TeamServiceImplementation implements TeamService {

    private static final Logger LOGGER = Logger.getLogger(TeamServiceImplementation.class.getName());
    private final DomainLookupService lookupService;

    /**
     * Constructs a new TeamServiceImplementation injecting the required lookup data utility interface.
     *
     * @param lookupService the centralized data lookup query provider service
     */
    public TeamServiceImplementation(DomainLookupService lookupService) {
        this.lookupService = Objects.requireNonNull(lookupService, "DomainLookupService dependency cannot be null.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Team createTeam(Company company, UUID managerEmployeeId) {
        Employee employee = lookupService.findEmployeeById(company, managerEmployeeId);

        // Cross-entity check: A manager can lead only one team across the company
        boolean isAlreadyManaging = company.getTeams().stream()
                .anyMatch(team -> team.getManager() != null && team.getManager().equals(employee));

        if (isAlreadyManaging) {
            throw new TeamAssignmentException("Manager '" + employee.getName() + "' is already leading another team.");
        }

        // The Team model's internal setter automatically checks role compliance (Position.MANAGER)
        Team newTeam = new Team(employee);

        company.addTeam(newTeam);

        // Background Audit Log Entry
        LOGGER.log(Level.INFO, "Corporate team structure successfully provisioned. Leader Name: ''{0}'', Assigned Team ID: {1}",
                new Object[]{employee.getName(), newTeam.getId()});

        return newTeam;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addMemberToTeam(Company company, UUID teamId, UUID employeeId) {
        Team targetTeam = lookupService.findTeamById(company, teamId);
        Employee employee = lookupService.findEmployeeById(company, employeeId);

        if (employee.getPosition() == Position.MANAGER) {
            throw new TeamAssignmentException(String.format(
                    "Security Block: Employee '%s' holds a MANAGER role and cannot be assigned as a standard member contributor.",
                    employee.getName()));
        }

        boolean alreadyInAnyTeam = company.getTeams().stream()
                .anyMatch(team -> team.getMembers().stream().anyMatch(member -> member.equals(employee)));

        if (alreadyInAnyTeam) {
            throw new TeamAssignmentException("Employee '" + employee.getName() + "' is already assigned to a team in this company.");
        }


        boolean result = targetTeam.addMember(employee);

        if (result) {
            // Background Audit Log Entry
            LOGGER.log(Level.INFO, "Contributor successfully appended to team pool. Member Name: ''{0}'', Target Team ID: {1}",
                    new Object[]{employee.getName(), teamId});
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean dissolveTeam(Company company, UUID teamId) {
        Team targetTeam = lookupService.findTeamById(company, teamId);
        String managerName = targetTeam.getManager().getName();

        boolean result = company.removeTeam(targetTeam);

        if (result) {
            LOGGER.log(Level.INFO, "Team structure successfully dissolved. Manager Name: ''{0}'', Dissolved Team ID: {1}",
                    new Object[]{managerName, teamId});
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeMemberFromTeam(Company company, UUID employeeId) {
        Objects.requireNonNull(employeeId, "Employee ID cannot be null.");
        Employee employee = lookupService.findEmployeeById(company, employeeId);

        // Policy Enforcement Check: This method explicitly filters out manager entities
        if (employee.getPosition() == Position.MANAGER) {
            throw new org.informatics.exceptions.TeamAssignmentException(String.format(
                    "Eviction rejected: Employee '%s' is a MANAGER and cannot be evicted via regular member pools.",
                    employee.getName()));
        }

        Team assignedTeam = company.getTeams().stream()
                .filter(team -> team.getMembers().contains(employee))
                .findFirst()
                .orElseThrow(() -> new org.informatics.exceptions.TeamAssignmentException(String.format(
                        "Eviction failed: Employee '%s' is not currently assigned to any active team.",
                        employee.getName())));

        boolean result = assignedTeam.removeMember(employee);

        if (result) {
            LOGGER.log(Level.INFO, "Contributor successfully evicted from team pool. Member Name: ''{0}'', Source Team ID: {1}",
                    new Object[]{employee.getName(), assignedTeam.getId()});
        }
        return result;
    }


}
