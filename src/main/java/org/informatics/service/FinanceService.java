package org.informatics.service;

import org.informatics.data.Company;
import org.informatics.data.enums.Position;

import java.math.BigDecimal;

/**
 * <p>Defines the contract for managing financial configurations, compensation floors, and baseline payroll analytics.</p>
 * <p>Following the Command track of the CQRS architectural pattern, this interface handles financial mutations
 * and operational analytical calculations while segregating data-view console printers.</p>
 */
public interface FinanceService {

    /**
     * <p>Maps an absolute minimum entry salary floor to a specific position enum constant configuration.</p>
     * <p>This floor serves as a defensive baseline boundary check during subsequent employee hiring workflows.</p>
     *
     * @param company  the active {@link Company} workspace aggregate root where the configuration will be mapped
     * @param position the professional {@link Position} role tier constant targeted for configuration
     * @param salary   the non-negative minimum baseline {@link BigDecimal} currency floor value to assign
     * @throws org.informatics.exceptions.InvalidSalaryException if the provided salary numerical value scale is negative
     * @throws NullPointerException                               if any of the provided object parameters are null
     */
    void setSalaryForPosition(Company company, Position position, BigDecimal salary);

    /**
     * <p>Traverses the contracts directory to count the number of personnel earning strictly above a given financial threshold.</p>
     *
     * @param company   the active {@link Company} workspace aggregate root containing the active employee contracts
     * @param threshold the evaluation boundary {@link BigDecimal} baseline value used to filter salaries
     * @return a long integer representing the total count of active workers whose compensation exceeds the threshold
     * @throws NullPointerException if either the company or the threshold references are null
     */
    long countEmployeesWithSalaryGreaterThan(Company company, BigDecimal threshold);

    /**
     * <p>Calculates the precise statistical average compensation mean for all active employees within a specific position tier.</p>
     * <p>The result is mathematically scaled and rounded half-up to exactly two decimal places. If no employees
     * are active within the requested position, a safe balance value of {@code BigDecimal.ZERO} is returned.</p>
     *
     * @param company  the active {@link Company} workspace aggregate root containing the employee registries
     * @param position the professional {@link Position} track targeted for average evaluation
     * @return a {@link BigDecimal} representing the rounded average salary mean for the position
     * @throws NullPointerException if either the company or the position references are null
     */
    BigDecimal calculateAverageSalaryForPosition(Company company, Position position);
}
