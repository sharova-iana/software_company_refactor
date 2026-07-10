package org.informatics.data;

import org.informatics.data.enums.Position;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Represents the central company data container and acts as the aggregate root for the domain.
 * Following Domain-Driven Design (DDD) principles, this rich domain model protects all internal
 * maps and sets via unmodifiable collection wrappers, shielding corporate records from arbitrary
 * external state corruption.
 */
public class Company implements Serializable {

    @Serial
    private static final long serialVersionUID = -4522042363144376177L;

    /**
     * The name of the company. Length must be between 2 and 100 characters.
     */
    private String name;

    /**
     * An internal map linking each professional position to its minimum baseline entry salary floor.
     */
    private final Map<Position, BigDecimal> positionMinimumSalaries;

    /**
     * The internal master set containing all active employment contracts within the company.
     */
    private final Set<Contract> contracts;

    /**
     * The internal set containing all established corporate teams within the company.
     */
    private final Set<Team> teams;

    /**
     * A sequential internal counter used to track and generate the next valid unique contract number.
     */
    private int contractCounter;

    /**
     * Constructs a new Company instance with a specified name and initializes its internal tracking structures.
     *
     * @param name the non-null corporate workspace name (2 to 100 characters long)
     * @throws IllegalArgumentException if the name length violates corporate boundary rules
     * @throws NullPointerException     if the provided name string is null
     */
    public Company(String name) {
        this.setName(name);
        this.positionMinimumSalaries = new EnumMap<>(Position.class);
        this.contracts = new HashSet<>();
        this.teams = new HashSet<>();
        this.contractCounter = 0;
    }

    /**
     * Gets the name of the company.
     *
     * @return the company name string
     */
    public String getName() {
        return name;
    }

    /**
     * Sets a new name for the company after verifying character string lengths.
     *
     * @param name the non-null company name string to assign (2 to 100 characters)
     * @throws IllegalArgumentException if the name length is shorter than 2 or longer than 100 characters
     */
    public void setName(String name) {
        Objects.requireNonNull(name, "Company name cannot be null.");
        String trimmedName = name.trim();

        if (trimmedName.length() < 2 || trimmedName.length() > 100) {
            throw new IllegalArgumentException("Invalid company name format. Workspace titles must be between 2 and 100 characters long.");
        }
        this.name = trimmedName;
    }

    /**
     * Gets a read-only view of the internal configuration map tracking baseline minimum position salaries.
     * Mutating this map directly from outside this class will throw an {@link UnsupportedOperationException}.
     *
     * @return an unmodifiable view of the {@link Map} tracking {@link Position} keys to their monetary {@link BigDecimal} floors
     */
    public Map<Position, BigDecimal> getPositionMinimumSalaries() {
        return Collections.unmodifiableMap(positionMinimumSalaries);
    }

    /**
     * Configures or updates the minimum entry salary floor mapped to a specific professional role tier.
     * Following strict domain invariants, this method completely blocks negative numerical bounds.
     *
     * @param position the professional position tier targeted for configuration mapping
     * @param salary   the non-null baseline compensation floor value to register
     * @throws org.informatics.exceptions.InvalidSalaryException if the provided salary numerical value scale is negative
     * @throws NullPointerException if either position or salary parameters are null
     */
    public void setSalaryForPosition(Position position, BigDecimal salary) {
        Objects.requireNonNull(position, "Position target cannot be null.");
        Objects.requireNonNull(salary, "Salary configuration floor reference cannot be null.");

        // Domain Protection Rule
        if (salary.compareTo(BigDecimal.ZERO) < 0) {
            throw new org.informatics.exceptions.InvalidSalaryException("Minimum salary floor configurations cannot fall below zero.");
        }

        this.positionMinimumSalaries.put(position, salary);
    }


    /**
     * Gets a read-only view of the master set containing all active employment contracts.
     * Mutating this set directly from outside this class will throw an {@link UnsupportedOperationException}.
     *
     * @return an unmodifiable view of the {@link Set} containing all active {@link Contract} records
     */
    public Set<Contract> getContracts() {
        return Collections.unmodifiableSet(contracts);
    }

    /**
     * Appends an active contract record directly into the company's internal contracts set ledger.
     *
     * @param contract the non-null contract record to add
     * @return {@code true} if the contract was successfully added; {@code false} if it was already present
     * @throws NullPointerException if the provided contract reference is null
     */
    public boolean addContract(Contract contract) {
        Objects.requireNonNull(contract, "Cannot register a null contract reference to the master registry.");
        return this.contracts.add(contract);
    }

    /**
     * Purges a contract record directly from the company's internal contracts set ledger.
     *
     * @param contract the non-null contract record to clear
     * @return {@code true} if the contract was found and removed; {@code false} otherwise
     * @throws NullPointerException if the provided contract reference is null
     */
    public boolean removeContract(Contract contract) {
        Objects.requireNonNull(contract, "Cannot purge a null contract reference from the master registry.");
        return this.contracts.remove(contract);
    }

    /**
     * Safely clears the internal active contracts registry and resets the sequential
     * contract tracking counter back to zero to prepare for a fresh payload data import.
     */
    public void clearContractsRegistry() {
        this.contracts.clear();
        this.contractCounter = 0;
    }


    /**
     * Gets a read-only view of the set containing all established corporate teams.
     * Mutating this set directly from outside this class will throw an {@link UnsupportedOperationException}.
     *
     * @return an unmodifiable view of the {@link Set} containing all established {@link Team} records
     */
    public Set<Team> getTeams() {
        return Collections.unmodifiableSet(teams);
    }

    /**
     * Appends an established corporate team shell directly into the company's internal teams listing registry.
     *
     * @param team the non-null team structural record to add
     * @return {@code true} if the team was successfully added; {@code false} if it was already present
     * @throws NullPointerException if the provided team reference is null
     */
    public boolean addTeam(Team team) {
        Objects.requireNonNull(team, "Cannot append a null team reference into the company registry.");
        return this.teams.add(team);
    }

    /**
     * Purges an established corporate team shell directly from the company's internal teams listing registry.
     *
     * @param team the non-null team structural record to clear
     * @return {@code true} if the team was found and removed; {@code false} otherwise
     * @throws NullPointerException if the provided team reference is null
     */
    public boolean removeTeam(Team team) {
        Objects.requireNonNull(team, "Cannot purge a null team reference from the company registry.");
        return this.teams.remove(team);
    }

    /**
     * Gets the current value of the internal contract sequence number tracking counter.
     *
     * @return the integer value representing the current highest contract sequence ID number
     */
    public int getContractCounter() {
        return contractCounter;
    }

    /**
     * Sets the value of the internal contract sequence number tracking counter manually.
     * This method is called during file restoration routines to synchronize incremental indices safely.
     *
     * @param contractCounter the new integer sequence value to assign to the counter
     */
    public void setContractCounter(int contractCounter) {
        this.contractCounter = contractCounter;
    }

    /**
     * Increments the underlying contract counter variable by 1 and returns the resulting value.
     *
     * @return the newly incremented sequential integer contract number value
     */
    public int incrementAndGetContractCounter() {
        return ++this.contractCounter;
    }

    /**
     * Compares this company with another object for equality based strictly on the company name.
     *
     * @param o the object to compare with
     * @return {@code true} if the objects have the same company name; {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Company company = (Company) o;
        return Objects.equals(name, company.name);
    }

    /**
     * Generates a hash code value for this company based on the company name.
     *
     * @return the integer hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    /**
     * Returns a multiline string representation of the company and all its structural states.
     *
     * @return a text summary containing the name, current contract count, salary maps, contracts, and teams
     */
    @Override
    public String toString() {
        return "Company{" +
                "name='" + name + '\'' +
                ", currentContractCounter=" + contractCounter +
                ", \n  positionSalaries=" + positionMinimumSalaries +
                ", \n  contracts=" + contracts +
                ", \n  teams=" + teams +
                '}';
    }
}
