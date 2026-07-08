package org.informatics.data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents an employment contract within the company.
 * Following Domain-Driven Design (DDD) principles, this rich domain model guards
 * its own invariants, ensuring that contract numbers remain strictly positive
 * and immutable throughout the system lifecycle.
 */
public class Contract implements Serializable {

    @Serial
    private static final long serialVersionUID = -6348162271809010579L;

    /**
     * The unique, sequential tracking number of this contract. Must be strictly positive.
     */
    private final int contractNumber;

    /**
     * The employee instance associated with this employment contract.
     */
    private final Employee employee;

    /**
     * Constructs a new Contract instance linking a number to an employee.
     * Enforces the corporate rule that contract sequence indices must be greater than zero.
     *
     * @param contractNumber the unique sequential number assigned to this contract (must be greater than zero)
     * @param employee       the non-null employee associated with this contract
     * @throws IllegalArgumentException if the contract number provided is zero or negative
     * @throws NullPointerException     if the provided employee instance reference is null
     */
    public Contract(int contractNumber, Employee employee) {
        if (contractNumber <= 0) {
            throw new IllegalArgumentException("Invalid contract format. Contract tracking numbers must be strictly positive integers.");
        }
        this.contractNumber = contractNumber;
        this.employee = Objects.requireNonNull(employee, "An employment contract cannot be instantiated without a valid employee.");
    }

    /**
     * Gets the contract number.
     *
     * @return the unique positive integer contract number
     */
    public int getContractNumber() {
        return contractNumber;
    }

    /**
     * Gets the employee associated with this contract.
     *
     * @return the {@link Employee} instance linked to this contract
     */
    public Employee getEmployee() {
        return employee;
    }

    /**
     * Compares this contract with another object for equality based strictly on the contract number.
     *
     * @param o the object to compare with
     * @return true if the objects have the same contract number; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contract contract = (Contract) o;
        return contractNumber == contract.contractNumber;
    }

    /**
     * Generates a hash code value for this contract based on the contract number.
     *
     * @return the integer hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(contractNumber);
    }

    /**
     * Returns a string representation of the contract and its attributes.
     *
     * @return a text summary containing the contract number and nested employee details
     */
    @Override
    public String toString() {
        return "Contract{" +
                "contractNumber=" + contractNumber +
                ", employee=" + employee +
                '}';
    }
}
