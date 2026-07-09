package org.informatics.service;

import org.informatics.data.Company;
import org.informatics.data.Team;

import java.util.UUID;

/**
 * <p>Defines the contract for managing corporate team units and relational organizational group structures.</p>
 * <p>Following the Command track of the CQRS architectural pattern, this interface groups structural mutations
 * and contributor assignments while segregating read-only view queries.</p>
 */
public interface TeamService {

    /**
     * <p>Establishes a fresh corporate team shell under the leadership of a designated manager contract.</p>
     * <p>This method enforces strict organizational checks: the contract must be verified, must hold the
     * required {@code Position.MANAGER} role tier, and cannot already lead another active team simultaneously.</p>
     *
     * @param company           the active {@link Company} workspace aggregate root instance where the team will be registered
     * @param managerEmployeeId the unique {@link UUID} identifier of the hired employee appointed to manage this team
     * @return the fully initialized, registered {@link Team} instance containing an auto-generated unique tracking token
     * @throws org.informatics.exceptions.EntityNotFoundException    if the provided manager ID does not match any active contract record
     * @throws org.informatics.exceptions.PositionMismatchException  if the selected employee's contract does not hold the required manager position tier
     * @throws org.informatics.exceptions.TeamAssignmentException   if the selected manager contract is already leading another corporate team
     * @throws NullPointerException                                  if either the company or the managerEmployeeId references are null
     */
    Team createTeam(Company company, UUID managerEmployeeId);

    /**
     * <p>Appends an active employment contract contributor to a specific team's member contract set.</p>
     * <p>This method enforces strict boundaries: contract agreements with a {@code Position.MANAGER} status are blocked
     * from assuming contributor roles, and a regular contract is restricted to joining a single team pool at a time.</p>
     *
     * @param company    the active {@link Company} workspace aggregate root containing the team and contract registries
     * @param teamId     the unique {@link UUID} tracking token of the target team shell to modify
     * @param employeeId the unique {@link UUID} tracking token of the employee candidate attempting to join the team
     * @return {@code true} if the employee contract is successfully added to the member set; {@code false} otherwise
     * @throws org.informatics.exceptions.EntityNotFoundException  if the target team ID or employee ID cannot be located in memory records
     * @throws org.informatics.exceptions.TeamAssignmentException if the contract is a manager, or is already active in another team pool
     * @throws NullPointerException                                if any of the provided parameter references are null
     */
    boolean addMemberToTeam(Company company, UUID teamId, UUID employeeId);

    /**
     * <p>Removes a manager from a designated team, resulting in the complete dissolution of the team structure.</p>
     * <p>This operation wipes the team instance entirely from the company's master directory list, while keeping
     * the underlying employee contracts and profiles completely untouched.</p>
     *
     * @param company the active {@link Company} workspace aggregate root containing the corporate team registries
     * @param teamId  the unique {@link UUID} tracking token of the team targeted for dissolution
     * @return {@code true} if the team shell is successfully purged from company records; {@code false} otherwise
     * @throws org.informatics.exceptions.EntityNotFoundException if the provided team ID does not match any active team registry
     * @throws NullPointerException                               if either the company or the teamId references are null
     */
    boolean dissolveTeam(Company company, UUID teamId);

    /**
     * <p>Evicts a regular contract contributor from a designated team pool based on their employee tracking ID.</p>
     *
     * @param company    the active {@link Company} workspace aggregate root containing the team registries
     * @param employeeId the unique {@link UUID} tracking token of the member contributor targeted for eviction
     * @return {@code true} if the employee contract is successfully removed from the team's member set; {@code false} otherwise
     * @throws org.informatics.exceptions.EntityNotFoundException if the target team ID or employee ID cannot be located in memory records
     * @throws NullPointerException                               if any of the provided parameter references are null
     */
    boolean removeMemberFromTeam(Company company, UUID employeeId);
}
