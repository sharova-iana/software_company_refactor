package org.informatics.data;

import org.informatics.data.enums.Gender;
import org.informatics.data.enums.Position;
import org.informatics.exceptions.AgeBoundaryException;
import org.informatics.exceptions.InvalidSalaryException;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a corporate employee within the enterprise.
 * Following Domain-Driven Design (DDD) principles, this rich domain model guards
 * its own invariants (internal structural rules), making it impossible for an employee
 * instance to ever exist in an invalid or corrupt state in memory.
 */
public class Employee implements Serializable {

    @Serial
    private static final long serialVersionUID = -6455762416962209192L;

    /**
     * The unique, immutable identifier for this employee.
     */
    private final UUID id;

    /**
     * The full name of the employee. Length must be between 2 and 100 characters.
     */
    private String name;

    /**
     * The gender of the employee.
     */
    private Gender gender;

    /**
     * The birth date of the employee. Enforces that the worker is between 18 and 70 years old.
     */
    private LocalDate birthDate;

    /**
     * The current professional position assigned to this employee.
     */
    private Position position;

    /**
     * The custom monthly salary amount of the employee. Must be non-negative.
     */
    private BigDecimal salary;

    /**
     * Constructs a new Employee instance and executes self-validation guard checks.
     * A unique {@link UUID} is automatically generated and assigned to the employee ID field.
     *
     * @param name             the full name of the employee (2 to 100 characters)
     * @param gender           the gender enumeration value of the employee
     * @param birthDate        the birth date of the employee (enforcing ages 18 to 70 inclusive)
     * @param position         the professional position of the employee
     * @param salary           the negotiated monthly salary amount (must be non-negative)
     * @throws IllegalArgumentException if the name length violates string boundary rules
     * @throws AgeBoundaryException    if the calculated chronological age is under 18 or over 70
     * @throws InvalidSalaryException  if the initial salary value provided is negative
     * @throws NullPointerException     if any of the mandatory parameter references are null
     */
    public Employee(String name, Gender gender, LocalDate birthDate, Position position, BigDecimal salary) {
        this.id = UUID.randomUUID();
        this.gender = Objects.requireNonNull(gender, "Gender cannot be null.");
        this.position = Objects.requireNonNull(position, "Position cannot be null.");

        // Delegate core parameter validations entirely to the setters
        this.setName(name);
        this.setBirthDate(birthDate);
        this.setSalary(salary);
    }

    /**
     * Gets the unique identifier of this employee.
     *
     * @return the {@link UUID} token assigned to this employee instance
     */
    public UUID getId() {
        return id;
    }

    /**
     * Gets the full name of the employee.
     *
     * @return the name string
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the full name of the employee after executing corporate string length reality checks.
     *
     * @param name the new name string to assign (must be between 2 and 100 characters)
     * @throws IllegalArgumentException if the name length is shorter than 2 or longer than 100 characters
     */
    public void setName(String name) {
        Objects.requireNonNull(name, "Name cannot be null.");
        String trimmedName = name.trim();

        if (trimmedName.length() < 2 || trimmedName.length() > 100) {
            throw new IllegalArgumentException("Invalid name format. Employee names must be between 2 and 100 characters long.");
        }
        this.name = trimmedName;
    }

    /**
     * Gets the gender of the employee.
     *
     * @return the {@link Gender} enum value
     */
    public Gender getGender() {
        return gender;
    }

    /**
     * Sets the gender of the employee.
     *
     * @param gender the new {@link Gender} enum value to assign
     */
    public void setGender(Gender gender) {
        this.gender = Objects.requireNonNull(gender, "Gender cannot be null.");
    }

    /**
     * Gets the birth date of the employee.
     *
     * @return the {@link LocalDate} instance representing the birth date
     */
    public LocalDate getBirthDate() {
        return birthDate;
    }

    /**
     * Sets the birth date of the employee after calculating chronological company age limits.
     *
     * @param birthDate the new {@link LocalDate} to assign (enforces age boundaries of 18 to 70)
     * @throws AgeBoundaryException if the resulting calculated age falls below 18 or exceeds 70 years old
     */
    public void setBirthDate(LocalDate birthDate) {
        Objects.requireNonNull(birthDate, "Birth date cannot be null.");

        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < 18 || age > 70) {
            throw new AgeBoundaryException(String.format(
                    "Hiring rejected. Age must be between 18 and 70. Provided birth date results in age: %d.", age));
        }
        this.birthDate = birthDate;
    }

    /**
     * Gets the professional position of the employee.
     *
     * @return the {@link Position} enum value
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Sets the professional position of the employee.
     *
     * @param position the new {@link Position} enum value to assign
     */
    public void setPosition(Position position) {
        this.position = Objects.requireNonNull(position, "Position cannot be null.");
    }

    /**
     * Gets the monthly salary amount of the employee.
     *
     * @return the {@link BigDecimal} numeric value representing the salary
     */
    public BigDecimal getSalary() {
        return salary;
    }

    /**
     * Sets the monthly salary amount of the employee after running non-negative financial baseline checks.
     *
     * @param salary the new {@link BigDecimal} numeric value to assign (must be non-negative)
     * @throws InvalidSalaryException if the provided compensation scale input is negative
     */
    public void setSalary(BigDecimal salary) {
        Objects.requireNonNull(salary, "Salary cannot be null.");

        if (salary.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidSalaryException("Employee compensation adjustments cannot fall below zero.");
        }
        this.salary = salary;
    }

    /**
     * Compares this employee with another object for equality based strictly on the unique ID.
     *
     * @param o the object to compare with
     * @return {@code true} if the objects have the same ID; {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(id, employee.id);
    }

    /**
     * Generates a hash code value for this employee based on their unique ID.
     *
     * @return the integer hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns a string representation of the employee and their attributes.
     *
     * @return a text summary containing the values of all fields
     */
    @Override
    public String toString() {
        return "Employee{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", gender=" + gender +
                ", birthDate=" + birthDate +
                ", position=" + position +
                ", salary=" + salary +
                '}';
    }
}
