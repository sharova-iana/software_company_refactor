package org.informatics.service.impl;

import org.informatics.data.Company;
import org.informatics.data.Contract;
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
 * <p>Provides the concrete business logic implementation for organizational team building and member coordination workflows.</p>
 * <p>This implementation manages structural contract-to-team assignments and writes audit logs to the file stream array.</p>
 */
public class TeamServiceImplementation implements TeamService {

    private static final Logger LOGGER = Logger.getLogger(TeamServiceImplementation.class.getName());
    private final DomainLookupService lookupService;

    /**
     * <p>Constructs a new TeamServiceImplementation injecting the required lookup data utility interface.</p>
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
        // Look up the active Contract wrapper to evaluate corporate status constraints
        Contract managerContract = lookupService.findContractByEmployeeId(company, managerEmployeeId);
        Employee employee = managerContract.getEmployee();

        // Cross-entity check: A manager contract can lead only one team across the company
        boolean isAlreadyManaging = company.getTeams().stream()
                .anyMatch(team -> team.getManagerContract() != null && team.getManagerContract().equals(managerContract));

        if (isAlreadyManaging) {
            throw new TeamAssignmentException("Manager '" + employee.getName() + "' is already leading another team.");
        }

        // The Team model constructor internally checks role compliance via managerContract.getPosition()
        Team newTeam = new Team(managerContract);
        company.addTeam(newTeam);

        LOGGER.log(Level.INFO, "Corporate team structure provisioned. Leader Name: ''{0}'', Assigned Team ID: {1}",
                new Object[]{employee.getName(), newTeam.getId()});

        return newTeam;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addMemberToTeam(Company company, UUID teamId, UUID employeeId) {
        Team targetTeam = lookupService.findTeamById(company, teamId);
        Contract memberContract = lookupService.findContractByEmployeeId(company, employeeId);
        Employee employee = memberContract.getEmployee();

        // Enforce role assignment blocks via contract configuration parameters
        if (memberContract.getPosition() == Position.MANAGER) {
            throw new TeamAssignmentException(String.format(
                    "Security Block: Employee '%s' holds a MANAGER contract and cannot be assigned as a contributor.",
                    employee.getName()));
        }

        // Ensure a regular contributor contract joins at most one team pool across the enterprise
        boolean alreadyInAnyTeam = company.getTeams().stream()
                .anyMatch(team -> team.getMemberContracts().stream().anyMatch(c -> c.equals(memberContract)));

        if (alreadyInAnyTeam) {
            throw new TeamAssignmentException("Employee '" + employee.getName() + "' is already assigned to a team in this company.");
        }

        boolean result = targetTeam.addMemberContract(memberContract);

        if (result) {
            LOGGER.log(Level.INFO, "Contributor contract appended to team pool. Member Name: ''{0}'', Target Team ID: {1}",
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
        String managerName = targetTeam.getManagerContract().getEmployee().getName();

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
        Contract memberContract = lookupService.findContractByEmployeeId(company, employeeId);
        Employee employee = memberContract.getEmployee();

        // Policy Enforcement Check: This method explicitly filters out manager contract entities
        if (memberContract.getPosition() == Position.MANAGER) {
            throw new TeamAssignmentException(String.format(
                    "Eviction rejected: Employee '%s' holds a MANAGER contract and cannot be evicted via member pools.",
                    employee.getName()));
        }

        // Scan teams to locate which specific set references this active legal contract
        Team assignedTeam = company.getTeams().stream()
                .filter(team -> team.getMemberContracts().contains(memberContract))
                .findFirst()
                .orElseThrow(() -> new TeamAssignmentException(String.format(
                        "Eviction failed: Employee '%s' is not currently assigned to any active team.",
                        employee.getName())));

        boolean result = assignedTeam.removeMemberContract(memberContract);

        if (result) {
            LOGGER.log(Level.INFO, "Contributor contract evicted from team pool. Member Name: ''{0}'', Source Team ID: {1}",
                    new Object[]{employee.getName(), assignedTeam.getId()});
        }
        return result;
    }
}
