package org.informatics.data;

import org.informatics.data.enums.Position;
import org.informatics.exceptions.InvalidSalaryException;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * <p>Represents an employment contract legal agreement within the company.</p>
 * <p>Following Domain-Driven Design (DDD) principles, this rich domain model guards
 * its own invariants, ensuring that contract tracking indices remain strictly positive
 * and that professional compensation and position terms are protected throughout the system lifecycle.</p>
 */
public class Contract implements Serializable {

    @Serial
    private static final long serialVersionUID = -2894104819482910482L;

    /**
     * The unique, sequential tracking number of this contract. Must be strictly positive.
     */
    private final int contractNumber;

    /**
     * The employee identity instance associated with this employment contract.
     */
    private final Employee employee;

    /**
     * The current professional role position tier assigned under this contract agreement.
     */
    private Position position;

    /**
     * The monthly salary compensation scale negotiated under this contract agreement.
     */
    private BigDecimal salary;

    /**
     * <p>Constructs a new Contract instance linking an identity to structural corporate rules.</p>
     * <p>Enforces that contract sequence indices must be greater than zero and that salaries
     * follow non-negative business boundaries.</p>
     *
     * @param contractNumber the unique sequential number assigned to this contract (must be greater than zero)
     * @param employee       the non-null employee associated with this contract
     * @param position       the initial professional role position assignment terms
     * @param salary         the initial negotiated monthly salary scale amount
     * @throws IllegalArgumentException if the contract number provided is zero or negative
     * @throws InvalidSalaryException  if the initial salary value provided falls below zero
     * @throws NullPointerException     if any of the mandatory parameter references are null
     */
    public Contract(int contractNumber, Employee employee, Position position, BigDecimal salary) {
        if (contractNumber <= 0) {
            throw new IllegalArgumentException("Invalid contract format. Contract tracking numbers must be strictly positive integers.");
        }
        this.contractNumber = contractNumber;
        this.employee = Objects.requireNonNull(employee, "An employment contract cannot be instantiated without a valid employee.");
        this.position = Objects.requireNonNull(position, "Position cannot be null.");

        // Delegate initial salary validation to the defensive setter
        this.setSalary(salary);
    }

    /**
     * Gets the contract number index tracker.
     *
     * @return the unique positive integer contract number
     */
    public int getContractNumber() {
        return contractNumber;
    }

    /**
     * Gets the employee identity linked to this contract agreement.
     *
     * @return the {@link Employee} instance associated with this contract
     */
    public Employee getEmployee() {
        return employee;
    }

    /**
     * Gets the active professional position assigned under this contract.
     *
     * @return the {@link Position} enum value constant
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Sets a new professional position tier under this contract tracking lifecycle.
     *
     * @param position the new {@link Position} enum value to assign
     */
    public void setPosition(Position position) {
        this.position = Objects.requireNonNull(position, "Position cannot be null.");
    }

    /**
     * Gets the active monthly salary compensation scale assigned under this contract.
     *
     * @return the {@link BigDecimal} numeric value representing the salary
     */
    public BigDecimal getSalary() {
        return salary;
    }

    /**
     * Sets the monthly salary compensation scale after running non-negative baseline checks.
     *
     * @param salary the new {@link BigDecimal} numeric value to assign (must be non-negative)
     * @throws InvalidSalaryException if the provided compensation scale input is negative
     */
    public void setSalary(BigDecimal salary) {
        Objects.requireNonNull(salary, "Salary cannot be null.");

        if (salary.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidSalaryException("Contract compensation adjustments cannot fall below zero.");
        }
        this.salary = salary;
    }

    /**
     * Compares this contract with another object for equality based strictly on the unique contract number.
     *
     * @param o the object to compare with
     * @return {@code true} if the objects have the same contract number; {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contract contract = (Contract) o;
        return contractNumber == contract.contractNumber;
    }

    /**
     * Generates a hash code value for this contract based on the unique contract number.
     *
     * @return the integer hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(contractNumber);
    }

    /**
     * Returns a string representation of the contract and its corporate attributes.
     *
     * @return a text summary containing the contract values and nested employee details
     */
    @Override
    public String toString() {
        return "Contract{" +
                "contractNumber=" + contractNumber +
                ", employee=" + employee +
                ", position=" + position +
                ", salary=" + salary +
                '}';
    }
}
