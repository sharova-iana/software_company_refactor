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
 * Represents a corporate team unit within the company.
 * Following Domain-Driven Design (DDD) principles, this rich domain model protects its member
 * collections via unmodifiable views and exposes explicit mutator methods to alter internal state
 * after external service-layer validations pass.
 */
public class Team implements Serializable {

    @Serial
    private static final long serialVersionUID = 6791652232209641501L;

    /**
     * The unique, immutable identifier for this team.
     */
    private final UUID id;

    /**
     * The manager leading this team.
     */
    private Employee manager;

    /**
     * The internal set of employees who are members of this team.
     */
    private final Set<Employee> members;

    /**
     * Constructs a new Team instance with an assigned manager leader.
     * This constructor guarantees that the team is born with a valid manager.
     * The provided employee must hold the Position.MANAGER role tier.
     *
     * @param manager the Employee designated to manage and lead this team
     * @throws NullPointerException if the provided manager instance is null at birth
     * @throws PositionMismatchException if the employee does not hold a MANAGER position tier
     */
    public Team(Employee manager) {
        Objects.requireNonNull(manager, "A team cannot be created without a manager.");

        this.id = UUID.randomUUID();
        this.members = new HashSet<>();

        // Delegate role verification directly to our setter to avoid code duplication
        this.setManager(manager);
    }

    /**
     * Gets the manager currently leading this team.
     *
     * @return the {@link Employee} manager instance, or {@code null} if the team is being dissolved
     */
    public Employee getManager() {
        return manager;
    }

    /**
     * Assigns a new manager to lead this team structure.
     * Following strict domain invariants, this setter completely blocks null assignments
     * and guarantees the employee holds a MANAGER role tier.
     *
     * @param manager the new non-null Employee manager to assign
     * @throws NullPointerException if the manager reference is null
     * @throws PositionMismatchException if the employee does not hold a MANAGER position tier
     */
    public void setManager(Employee manager) {
        Objects.requireNonNull(manager, "A team manager reference cannot be null.");
        if (manager.getPosition() != Position.MANAGER) {
            throw new PositionMismatchException(
                    manager.getName(), Position.MANAGER, manager.getPosition());
        }
        this.manager = manager;
    }


    /**
     * Gets a read-only view of the employees who are members of this team.
     * Mutating this set directly from outside this class will throw an {@link UnsupportedOperationException}.
     *
     * @return an unmodifiable {@link Set} containing all {@link Employee} team members
     */
    public Set<Employee> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    /**
     * Appends an employee to the team's internal member collection.
     * This method is called by service orchestrators after cross-entity tracking validation pass.
     *
     * @param employee the non-null employee contributor to add
     * @return {@code true} if the employee was successfully added; {@code false} if they were already present
     * @throws NullPointerException if the provided employee reference is null
     */
    public boolean addMember(Employee employee) {
        Objects.requireNonNull(employee, "Cannot add a null employee to the team pool.");
        return this.members.add(employee);
    }

    /**
     * Removes an employee from the team's internal member collection.
     * This method is called by service orchestrators after business tracking validations pass.
     *
     * @param employee the non-null employee contributor to remove
     * @return {@code true} if the employee was found and removed; {@code false} otherwise
     * @throws NullPointerException if the provided employee reference is null
     */
    public boolean removeMember(Employee employee) {
        Objects.requireNonNull(employee, "Cannot remove a null employee from the team pool.");
        return this.members.remove(employee);
    }

    /**
     * Gets the unique identifier of this team.
     *
     * @return the {@link UUID} token assigned to this team instance
     */
    public UUID getId() {
        return id;
    }

    /**
     * Comapres this team with another object for equality based strictly on the unique ID.
     *
     * @param o the object to compare with
     * @return {@code true} if the objects have the same ID; {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return Objects.equals(id, team.id);
    }

    /**
     * Generates a hash code value for this team based on its unique ID.
     *
     * @return the integer hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    /**
     * Returns a string representation of the team and its attributes.
     *
     * @return a text summary containing the team ID, manager details, and employee members list
     */
    @Override
    public String toString() {
        return "Team{" +
                "id=" + id +
                ", manager=" + manager +
                ", members=" + members +
                '}';
    }
}
