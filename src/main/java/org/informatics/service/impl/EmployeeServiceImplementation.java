package org.informatics.service.impl;

import org.informatics.data.Company;
import org.informatics.data.Contract;
import org.informatics.data.Employee;
import org.informatics.data.enums.Gender;
import org.informatics.data.enums.Position;
import org.informatics.exceptions.SalaryConfigurationException;
import org.informatics.exceptions.InvalidSalaryException;
import org.informatics.service.EmployeeService;
import org.informatics.service.util.DomainLookupService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides the concrete business logic implementation for human resources employee lifecycle workflows.
 * This implementation manages personnel state mutations and writes tracking audit logs to the system log stream.
 */
public class EmployeeServiceImplementation implements EmployeeService {

    private static final Logger LOGGER = Logger.getLogger(EmployeeServiceImplementation.class.getName());
    private final DomainLookupService lookupService;

    /**
     * Constructs a new EmployeeServiceImplementation injecting the required lookup data utility interface.
     *
     * @param lookupService the centralized data lookup query provider service
     */
    public EmployeeServiceImplementation(DomainLookupService lookupService) {
        this.lookupService = Objects.requireNonNull(lookupService, "DomainLookupService dependency cannot be null.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Employee hireEmployee(Company company, String name, Gender gender, LocalDate birthDate, Position position, BigDecimal negotiatedSalary) {
        Objects.requireNonNull(company, "Company cannot be null.");
        Objects.requireNonNull(negotiatedSalary, "Negotiated salary cannot be null.");

        // The Employee data model guards its own age bounds and name lengths internally
        BigDecimal minFloor = company.getPositionMinimumSalaries().get(position);
        if (minFloor == null) {
            throw new SalaryConfigurationException("Cannot hire. No entry-level salary baseline configured for position: " + position);
        }

        if (negotiatedSalary.compareTo(minFloor) < 0) {
            throw new InvalidSalaryException(String.format(
                    "Rejected: Proposed salary %s is below the minimum baseline entry bound of %s for a %s.",
                    negotiatedSalary, minFloor, position));
        }

        Employee employee = new Employee(name, gender, birthDate, position, negotiatedSalary);
        int contractNumber = company.incrementAndGetContractCounter();
        Contract contract = new Contract(contractNumber, employee);

        company.addContract(contract);

        // Background Audit Log Entry
        LOGGER.log(Level.INFO, "Personnel record successfully provisioned. Name: ''{0}'', Assigned Contract ID: {1}",
                new Object[]{employee.getName(), contractNumber});

        return employee;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean fireEmployee(Company company, UUID employeeId) {
        Contract targetContract = lookupService.findContractByEmployeeId(company, employeeId);
        Employee employee = targetContract.getEmployee();

        // 1. Core Data Disconnect: Wipe the contract from the ledger
        company.removeContract(targetContract);

        // 2. Direct, Optimized Structural Cleanup without looping lookups
        if (employee.getPosition() == Position.MANAGER) {
            // Path A: Directly remove the team from the company collection using references we already hold
            company.getTeams().stream()
                    .filter(team -> team.getManager().equals(employee))
                    .findFirst()
                    .ifPresent(company::removeTeam);
        } else {
            // Path B: Directly remove the member from their specific team using references we already hold
            company.getTeams().stream()
                    .filter(team -> team.getMembers().contains(employee))
                    .findFirst()
                    .ifPresent(team -> team.removeMember(employee));
        }

        LOGGER.log(Level.INFO, "Personnel record successfully terminated. Name: ''{0}'', Revoked Contract ID: {1}",
                new Object[]{employee.getName(), targetContract.getContractNumber()});

        return true;
    }

}
