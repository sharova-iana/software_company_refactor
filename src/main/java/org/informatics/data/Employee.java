package org.informatics.data;

import org.informatics.data.enums.Gender;
import org.informatics.exceptions.AgeBoundaryException;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * <p>Represents a corporate employee entity within the enterprise layout.</p>
 * <p>Following pure Domain-Driven Design (DDD) principles, this rich domain model guards
 * its own internal structural invariants, making it impossible for a human employee
 * instance to ever be initialized or mutated into an invalid state in memory.</p>
 */
public class Employee implements Serializable {

    @Serial
    private static final long serialVersionUID = 4829104719482910482L;

    /**
     * Strict RFC-compliant email validation pattern used to protect string structures.
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    /**
     * The unique, immutable identifier for this employee.
     */
    private final UUID id;

    /**
     * The immutable chronological birth date of the employee.
     * Enforces at construction that the worker is between 18 and 70 years old.
     */
    private final LocalDate birthDate;

    /**
     * The full name of the employee. Length must be between 2 and 100 characters.
     */
    private String name;

    /**
     * The unique corporate communication email address of the employee.
     */
    private String email;

    /**
     * The gender of the employee.
     */
    private Gender gender;

    /**
     * <p>Constructs a new Employee identity instance and executes self-validation guard checks.</p>
     * <p>A unique {@link UUID} tracking token is automatically generated and assigned upon creation.</p>
     *
     * @param name      the full name of the employee (2 to 100 characters)
     * @param email     the unique communication email address matching standard format rules
     * @param gender    the biological gender enumeration value of the employee
     * @param birthDate the unchangeable birth date of the employee (enforcing ages 18 to 70 inclusive)
     * @throws IllegalArgumentException if the name length or email format violates layout bounds
     * @throws AgeBoundaryException    if the calculated chronological age is under 18 or over 70
     * @throws NullPointerException     if any of the mandatory parameter references are null
     */
    public Employee(String name, String email, Gender gender, LocalDate birthDate) {
        this.id = UUID.randomUUID();
        this.gender = Objects.requireNonNull(gender, "Gender cannot be null.");

        // Validate immutable birth date directly at constructor entry point
        Objects.requireNonNull(birthDate, "Birth date cannot be null.");
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < 18 || age > 70) {
            throw new AgeBoundaryException(String.format(
                    "Hiring rejected. Age must be between 18 and 70. Provided birth date results in age: %d.", age));
        }
        this.birthDate = birthDate;

        // Delegate remaining mutable parameter validations securely to the setters
        this.setName(name);
        this.setEmail(email);
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
     * Gets the unchangeable birth date of the employee.
     *
     * @return the {@link LocalDate} instance representing the birth date
     */
    public LocalDate getBirthDate() {
        return birthDate;
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
     * Sets the full name of the employee after executing corporate string length checks.
     *
     * @param name the new name string to assign (must be between 2 and 100 characters)
     * @throws IllegalArgumentException if the name length is shorter than 2 or longer than 100 characters
     */
    public void setName(String name) {
        Objects.requireNonNull(name, "Name cannot be null.");
        String trimmedName = name.trim();

        if (!isValidName(name) ){
            throw new IllegalArgumentException("Invalid name format. Employee names must be between 2 and 100 characters long.");
        }
        this.name = trimmedName;
    }

    /**
     * Gets the unique corporate communication email address of the employee.
     *
     * @return the email string mapping parameter
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the corporate communication email address after validating it against a strict regular expression rule.
     *
     * @param email the new email address to map (must conform to standard alphanumeric domain layout templates)
     * @throws IllegalArgumentException if the email structure fails the structural regex match parameter
     */
    public void setEmail(String email) {
        Objects.requireNonNull(email, "Email cannot be null.");
        String processedEmail = email.trim().toLowerCase();

        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format syntax. Please enter a valid corporate email structure (e.g., worker@company.com).");
        }
        this.email = processedEmail;
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
     * <p>Evaluates whether a raw string name conforms precisely to the system character length rules.</p>
     *
     * @param name the raw text string to evaluate
     * @return {@code true} if the trimmed length is between 2 and 100 characters inclusive; {@code false} otherwise
     */
    public static boolean isValidName(String name) {
        if (name == null) return false;
        String trimmed = name.trim();
        return trimmed.length() >= 2 && trimmed.length() <= 100;
    }


    /**
     * <p>Evaluates whether a raw string handle conforms precisely to the system email validation rules.</p>
     *
     * @param email the raw text string to evaluate
     * @return {@code true} if the syntax matches standard template rules; {@code false} otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL_PATTERN.matcher(email.trim().toLowerCase()).matches();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(id, employee.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", gender=" + gender +
                ", birthDate=" + birthDate +
                '}';
    }
}
