package org.informatics.data;

import org.informatics.data.enums.Position;
import org.informatics.exceptions.PositionMismatchException;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * <p>Represents a corporate team unit within the company layout.</p>
 * <p>Following pure Domain-Driven Design (DDD) principles, this rich domain model protects its member
 * collections via unmodifiable views and exposes explicit mutator methods to alter internal state.</p>
 * <p>Following our contract-centric domain shift, this class manages relationship structures using
 * active {@link Contract} links instead of raw human identities, ensuring that professional role tiers
 * remain fully verifiable at runtime.</p>
 */
public class Team implements Serializable {

    @Serial
    private static final long serialVersionUID = 6791652232209641501L;

    /**
     * The unique, immutable identifier for this team.
     */
    private final UUID id;

    /**
     * The employment contract agreement governing the manager leading this team.
     */
    private Contract managerContract;

    /**
     * The internal set of contract agreements governing the members assigned to this team.
     */
    private final Set<Contract> memberContracts;

    /**
     * <p>Constructs a new Team instance with an assigned manager contract leader.</p>
     * <p>This constructor guarantees that the team is born with a legally valid manager contract
     * holding the explicit {@link Position#MANAGER} role tier constant.</p>
     *
     * @param managerContract the Contract designated to manage and lead this team unit
     * @throws NullPointerException      if the provided manager contract instance is null at birth
     * @throws PositionMismatchException if the contract terms do not specify a MANAGER position tier
     */
    public Team(Contract managerContract) {
        Objects.requireNonNull(managerContract, "A team cannot be created without a manager contract.");

        this.id = UUID.randomUUID();
        this.memberContracts = new HashSet<>();

        // Delegate role verification directly to our setter to maintain code reuse rules
        this.setManagerContract(managerContract);
    }

    /**
     * Gets the contract agreement of the manager currently leading this team.
     *
     * @return the {@link Contract} manager instance
     */
    public Contract getManagerContract() {
        return managerContract;
    }

    /**
     * Assigns a new manager contract to lead this corporate team structure.
     * Following strict domain invariants, this setter completely blocks null assignments
     * and guarantees that the contract holds an active MANAGER role tier constant.
     *
     * @param managerContract the new non-null Contract manager agreement to assign
     * @throws NullPointerException      if the manager contract reference is null
     * @throws PositionMismatchException if the contract terms do not hold a MANAGER position tier
     */
    public void setManagerContract(Contract managerContract) {
        Objects.requireNonNull(managerContract, "A team manager contract reference cannot be null.");

        if (managerContract.getPosition() != Position.MANAGER) {
            throw new PositionMismatchException(
                    managerContract.getEmployee().getName(),
                    Position.MANAGER,
                    managerContract.getPosition()
            );
        }
        this.managerContract = managerContract;
    }

    /**
     * Gets a read-only view of the employee contracts who are members of this team.
     * Mutating this set directly from outside this class will throw an {@link UnsupportedOperationException}.
     *
     * @return an unmodifiable {@link Set} containing all {@link Contract} team members
     */
    public Set<Contract> getMemberContracts() {
        return Collections.unmodifiableSet(memberContracts);
    }

    /**
     * Appends an employee contract to the team's internal member collection view.
     *
     * @param memberContract the non-null contract contributor agreement to append
     * @return {@code true} if the contract was successfully added; {@code false} if it was already present
     * @throws NullPointerException if the provided contract reference is null
     */
    public boolean addMemberContract(Contract memberContract) {
        Objects.requireNonNull(memberContract, "Cannot add a null contract reference to the team pool.");
        return this.memberContracts.add(memberContract);
    }

    /**
     * Removes an employee contract from the team's internal member collection view.
     *
     * @param memberContract the non-null contract contributor agreement to remove
     * @return {@code true} if the contract was found and removed; {@code false} otherwise
     * @throws NullPointerException if the provided contract reference is null
     */
    public boolean removeMemberContract(Contract memberContract) {
        Objects.requireNonNull(memberContract, "Cannot remove a null contract reference from the team pool.");
        return this.memberContracts.remove(memberContract);
    }

    /**
     * Gets the unique identifier of this team unit.
     *
     * @return the {@link UUID} token assigned to this team instance
     */
    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return Objects.equals(id, team.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Team{" +
                "id=" + id +
                ", managerContract=" + managerContract +
                ", memberContracts=" + memberContracts +
                '}';
    }
}
