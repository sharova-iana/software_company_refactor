package org.informatics.service.util;

import org.informatics.data.Company;
import org.informatics.data.Contract;
import org.informatics.data.Employee;
import org.informatics.data.Team;

import java.util.UUID;

/**
 * <p>Defines the contract for executing stateless, read-only entity queries from company registries.</p>
 * <p>This service isolates raw data lookups into a shared, centralized utility layer. It prevents
 * tight coupling and circular dependency injection loops across human resources, team structure,
 * and reporting modules.</p>
 */
public interface DomainLookupService {

    /**
     * <p>Queries the active company registries to locate an employee by their unique tracking ID.</p>
     *
     * @param company    the active {@link Company} aggregate root instance containing the contract listings
     * @param employeeId the unique {@link UUID} tracking token of the target employee to discover
     * @return the verified, matching {@link Employee} reference found in memory
     * @throws org.informatics.exceptions.EntityNotFoundException if the provided employee ID does not map to any active contract record
     * @throws NullPointerException                               if either the company or the employeeId references are null
     */
    Employee findEmployeeById(Company company, UUID employeeId);

    /**
     * <p>Queries the active company registries to locate an established team by its unique tracking ID.</p>
     *
     * @param company the active {@link Company} aggregate root instance containing the team listings
     * @param teamId  the unique {@link UUID} tracking token of the target team to discover
     * @return the verified, matching {@link Team} reference found in memory
     * @throws org.informatics.exceptions.EntityNotFoundException if the provided team ID does not map to any established team record
     * @throws NullPointerException                               if either the company or the teamId references are null
     */
    Team findTeamById(Company company, UUID teamId);

    /**
     * <p>Queries the active company registries to find an active employment contract wrapping a specific employee ID.</p>
     *
     * @param company    the active {@link Company} aggregate root instance containing the contract listings
     * @param employeeId the unique {@link UUID} tracking token of the employee whose contract is targeted
     * @return the verified, matching {@link Contract} reference instance wrapping the employee context
     * @throws org.informatics.exceptions.EntityNotFoundException if no active contract reference maps to the provided employee ID
     * @throws NullPointerException                               if either the company or the employeeId references are null
     */
    Contract findContractByEmployeeId(Company company, UUID employeeId);
}
