package org.informatics.data;

import org.informatics.data.Employee;
import org.informatics.data.enums.Gender;
import org.informatics.data.enums.Position;
import org.informatics.exceptions.AgeBoundaryException;
import org.informatics.exceptions.InvalidSalaryException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Executes pure Detroit-style classicist unit tests for the Employee data model invariants.
 * Evaluates core parameter thresholds, name limits, and labor boundaries without any Mockito stubs.
 */
class EmployeeDataModelUnitTest {

    // =========================================================================
    // METHOD UNDER TEST: Employee (Constructor)
    // =========================================================================

    @Test
    void testConstructor_shouldInstantiateCorrectly_whenArgumentsAreValid() {
        // given
        String validName = "Alice Brown";
        LocalDate validBirthDate = LocalDate.now().minusYears(25);
        BigDecimal validSalary = new BigDecimal("3500.00");

        // when
        Employee employee = new Employee(validName, Gender.FEMALE, validBirthDate, Position.JUNIOR_DEVELOPER, validSalary);

        // then
        assertNotNull(employee.getId(), "Every employee must be born with an initialized final UUID token");
        assertEquals("Alice Brown", employee.getName());
        assertEquals(Gender.FEMALE, employee.getGender());
        assertEquals(validBirthDate, employee.getBirthDate());
        assertEquals(Position.JUNIOR_DEVELOPER, employee.getPosition());
        assertEquals(0, validSalary.compareTo(employee.getSalary()));
    }

    @Test
    void testConstructor_shouldThrowIllegalArgumentException_whenNameIsShorterThanTwoCharacters() {
        // given
        String corruptShortName = "A";
        LocalDate validBirthDate = LocalDate.now().minusYears(30);
        BigDecimal validSalary = new BigDecimal("4000.00");

        // when/then
        assertThrows(IllegalArgumentException.class, () ->
                        new Employee(corruptShortName, Gender.MALE, validBirthDate, Position.SENIOR_DEVELOPER, validSalary),
                "Should reject names with sub-boundary character sizes"
        );
    }

    @Test
    void testConstructor_shouldThrowAgeBoundaryException_whenApplicantIsUnderage() {
        // given
        String name = "Underage Worker";
        LocalDate underageBirthDate = LocalDate.now().minusYears(16); // 16 years old
        BigDecimal validSalary = new BigDecimal("2000.00");

        // when/then
        assertThrows(AgeBoundaryException.class, () ->
                        new Employee(name, Gender.MALE, underageBirthDate, Position.JUNIOR_DEVELOPER, validSalary),
                "Should reject creating employees below the lower age variant of 18"
        );
    }

    @Test
    void testConstructor_shouldThrowInvalidSalaryException_whenInitialSalaryIsNegative() {
        // given
        String name = "Unpaid Worker";
        LocalDate validBirthDate = LocalDate.now().minusYears(40);
        BigDecimal negativeSalary = new BigDecimal("-1.50");

        // when/then
        assertThrows(InvalidSalaryException.class, () ->
                        new Employee(name, Gender.MALE, validBirthDate, Position.QA_ENGINEER, negativeSalary),
                "Should reject initializing objects with negative financials"
        );
    }

}
