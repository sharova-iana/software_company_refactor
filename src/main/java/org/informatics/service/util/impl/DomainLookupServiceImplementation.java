package org.informatics.service.util.impl;

import org.informatics.data.Company;
import org.informatics.data.Contract;
import org.informatics.data.Employee;
import org.informatics.data.Team;
import org.informatics.exceptions.EntityNotFoundException;
import org.informatics.service.util.DomainLookupService;

import java.util.Objects;
import java.util.UUID;

/**
 * <p>Provides the concrete implementation for executing stateless, read-only entity lookup queries.</p>
 * <p>This utility module handles stream traversals over active contracts and teams without modifying memory state.</p>
 */
public class DomainLookupServiceImplementation implements DomainLookupService {

    /**
     * {@inheritDoc}
     */
    @Override
    public Employee findEmployeeById(Company company, UUID employeeId) {
        Objects.requireNonNull(company, "Company reference cannot be null.");
        Objects.requireNonNull(employeeId, "Employee ID token cannot be null.");

        return company.getContracts().stream()
                .map(Contract::getEmployee)
                .filter(emp -> emp.getId().equals(employeeId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("No employee found with ID: " + employeeId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Team findTeamById(Company company, UUID teamId) {
        Objects.requireNonNull(company, "Company reference cannot be null.");
        Objects.requireNonNull(teamId, "Team ID token cannot be null.");

        return company.getTeams().stream()
                .filter(team -> team.getId().equals(teamId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("No team found with ID: " + teamId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Contract findContractByEmployeeId(Company company, UUID employeeId) {
        Objects.requireNonNull(company, "Company reference cannot be null.");
        Objects.requireNonNull(employeeId, "Employee ID token cannot be null.");

        return company.getContracts().stream()
                .filter(contract -> contract.getEmployee().getId().equals(employeeId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("No active employment contract found for Employee ID: " + employeeId));
    }
}
