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

public class EmployeeServiceImplementation implements EmployeeService {

    private static final Logger LOGGER = Logger.getLogger(EmployeeServiceImplementation.class.getName());
    private final DomainLookupService lookupService;

    /**
     * <p>Constructs a new EmployeeServiceImplementation injecting the required lookup data utility interface.</p>
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
    public Contract hireEmployee(Company company, String name, String email, Gender gender, LocalDate birthDate, Position position, BigDecimal negotiatedSalary) {
        Objects.requireNonNull(company, "Company cannot be null.");
        Objects.requireNonNull(negotiatedSalary, "Negotiated salary cannot be null.");

        BigDecimal minFloor = company.getPositionMinimumSalaries().get(position);
        if (minFloor == null) {
            throw new SalaryConfigurationException("Cannot hire. No entry-level salary baseline configured for position: " + position);
        }

        if (negotiatedSalary.compareTo(minFloor) < 0) {
            throw new InvalidSalaryException(String.format(
                    "Rejected: Proposed salary %s is below the minimum baseline entry bound of %s for a %s.",
                    negotiatedSalary, minFloor, position));
        }

        Employee employee = new Employee(name, email, gender, birthDate);
        int contractNumber = company.incrementAndGetContractCounter();
        Contract contract = new Contract(contractNumber, employee, position, negotiatedSalary);
        company.addContract(contract);

        LOGGER.log(Level.INFO, "Personnel record provisioned. Name: ''{0}'', Email: ''{1}'', Contract ID: {2}",
                new Object[]{employee.getName(), employee.getEmail(), contractNumber});

        return contract;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean fireEmployee(Company company, UUID employeeId) {
        Contract targetContract = lookupService.findContractByEmployeeId(company, employeeId);
        Employee employee = targetContract.getEmployee();

        company.removeContract(targetContract);

        if (targetContract.getPosition() == Position.MANAGER) {
            company.getTeams().stream()
                    .filter(team -> team.getManagerContract().equals(targetContract))
                    .findFirst()
                    .ifPresent(company::removeTeam);
        } else {
            company.getTeams().stream()
                    .filter(team -> team.getMemberContracts().contains(targetContract))
                    .findFirst()
                    .ifPresent(team -> team.removeMemberContract(targetContract));
        }

        LOGGER.log(Level.INFO, "Personnel contract terminated. Name: ''{0}'', Email: ''{1}'', Revoked ID: {2}",
                new Object[]{employee.getName(), employee.getEmail(), targetContract.getContractNumber()});

        return true;
    }
}
