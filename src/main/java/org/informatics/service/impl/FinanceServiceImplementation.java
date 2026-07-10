package org.informatics.service.impl;

import org.informatics.data.Company;
import org.informatics.data.Contract;
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
 * <p>Provides the concrete business logic implementation for payroll metrics, salary baselines, and financial evaluations.</p>
 * <p>Following our contract-centric domain shift, this implementation queries financial terms directly off legal agreements
 * rather than human identity descriptors.</p>
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

        company.setSalaryForPosition(position, salary);

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

        // Refactored to traverse the contract properties layer directly
        return company.getContracts().stream()
                .filter(contract -> contract.getSalary().compareTo(threshold) > 0)
                .count();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal calculateAverageSalaryForPosition(Company company, Position position) {
        Objects.requireNonNull(company, "Company cannot be null.");
        Objects.requireNonNull(position, "Position cannot be null.");

        // Refactored to isolate and filter matching active contracts instead of nested profiles
        List<Contract> matchingContracts = company.getContracts().stream()
                .filter(contract -> contract.getPosition() == position)
                .toList();

        if (matchingContracts.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Sum contract salaries up directly in RAM memory streams
        BigDecimal totalSalary = matchingContracts.stream()
                .map(Contract::getSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalSalary.divide(BigDecimal.valueOf(matchingContracts.size()), 2, RoundingMode.HALF_UP);
    }
}
