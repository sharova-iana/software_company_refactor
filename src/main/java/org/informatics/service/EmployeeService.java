package org.informatics.service;

import org.informatics.data.Company;
import org.informatics.data.Employee;
import org.informatics.data.enums.Gender;
import org.informatics.data.enums.Position;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * <p>Defines the contract for managing the lifecycle of employees within the enterprise.</p>
 * <p>Following the Command track of the CQRS architectural pattern, this interface segregates
 * human resources operations that mutate state from informational read queries.</p>
 */
public interface EmployeeService {

    /**
     * <p>Hires a new employee into the company workspace, creating their profile and
     * generating a sequential employment contract line automatically.</p>
     * <p>This method enforces mandatory business rules: the applicant's age must be
     * valid, a baseline salary floor must be configured for the requested role, and
     * the negotiated compensation must meet or exceed that floor boundary.</p>
     *
     * @param company          the active {@link Company} workspace aggregate root instance where the employee will be hired
     * @param name             the full alphabet text string name profile of the employee
     * @param gender           the biological {@link Gender} enumeration designation of the employee
     * @param birthDate        the calendar {@link LocalDate} birthdate context used for compliance age calculations
     * @param position         the active professional {@link Position} role tier configuration assigned to the worker
     * @param negotiatedSalary the precise high-utility monetary {@link BigDecimal} numeric value scale negotiated
     * @return the fully initialized, reconstructed {@link Employee} instance containing an auto-generated unique ID
     * @throws org.informatics.exceptions.AgeBoundaryException         if the calculated age of the applicant is under 18 or over 70 years old
     * @throws org.informatics.exceptions.SalaryConfigurationException if the target position does not have a baseline minimum floor set up
     * @throws org.informatics.exceptions.InvalidSalaryException       if the proposed negotiated salary falls below the minimum position floor
     * @throws NullPointerException                                    if any of the provided object parameter references are null
     */
    Employee hireEmployee(Company company, String name, Gender gender, LocalDate birthDate, Position position, BigDecimal negotiatedSalary);

    /**
     * <p>Terminates an employee contract and completely purges their active references from central records.</p>
     * <p>This operation cascades down to automatically evict the terminated worker from any active regular contributor
     * member sets across all teams, or triggers team dissolution sequences if the employee holds a manager slot.</p>
     *
     * @param company    the active {@link Company} workspace aggregate root containing the employment registries
     * @param employeeId the unique {@link UUID} tracking token of the employee targeted for termination
     * @return {@code true} if the termination cleanup cycle completes successfully; {@code false} otherwise
     * @throws org.informatics.exceptions.EntityNotFoundException if no active contract reference maps to the provided employee ID
     * @throws NullPointerException                               if either the company or the employeeId references are null
     */
    boolean fireEmployee(Company company, UUID employeeId);
}
