package org.informatics.service.impl;

import org.informatics.data.Company;
import org.informatics.data.Contract;
import org.informatics.data.Employee;
import org.informatics.data.enums.Position;
import org.informatics.exceptions.InvalidSalaryException;
import org.informatics.service.FinanceService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides the concrete business logic implementation for payroll metrics, salary baselines, and financial evaluations.
 * This implementation manages data mutations for position scales and records tracking audit logs to the log stream.
 */
public class FinanceServiceImplementation implements FinanceService {

    private static final Logger LOGGER = Logger.getLogger(FinanceServiceImplementation.class.getName());

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSalaryForPosition(Company company, Position position, BigDecimal salary) {
        Objects.requireNonNull(company, "Company cannot be null.");
        Objects.requireNonNull(position, "Position cannot be null.");
        Objects.requireNonNull(salary, "Salary cannot be null.");

        // Financial business validation check
        if (salary.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidSalaryException("Minimum salary floor cannot be negative.");
        }

        // Telling the Company aggregate root to perform the assignment internally
        company.setSalaryForPosition(position, salary);

        // Background Audit Log Entry
        LOGGER.log(Level.INFO, "Salary threshold configuration successfully updated. Position: ''{0}'', Minimum Floor Assigned: {1}",
                new Object[]{position.name(), salary});
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public long countEmployeesWithSalaryGreaterThan(Company company, BigDecimal threshold) {
        Objects.requireNonNull(company, "Company cannot be null.");
        Objects.requireNonNull(threshold, "Threshold cannot be null.");

        return company.getContracts().stream()
                .map(Contract::getEmployee)
                .filter(employee -> employee.getSalary().compareTo(threshold) > 0)
                .count();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal calculateAverageSalaryForPosition(Company company, Position position) {
        Objects.requireNonNull(company, "Company cannot be null.");
        Objects.requireNonNull(position, "Position cannot be null.");

        List<Employee> matchingEmployees = company.getContracts().stream()
                .map(Contract::getEmployee)
                .filter(emp -> emp.getPosition() == position)
                .toList();

        if (matchingEmployees.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalSalary = matchingEmployees.stream()
                .map(Employee::getSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalSalary.divide(BigDecimal.valueOf(matchingEmployees.size()), 2, RoundingMode.HALF_UP);
    }
}
