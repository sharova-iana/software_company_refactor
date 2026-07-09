package org.informatics.data;

import org.informatics.data.enums.Gender;
import org.informatics.exceptions.AgeBoundaryException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;


class EmployeeDataModelUnitTest {

    // =========================================================================
    // METHOD UNDER TEST: Employee (Constructor)
    // =========================================================================

    @Test
    void testConstructor_shouldInstantiateCorrectly_whenArgumentsAreValid() {
        String validName = "Alice Brown";
        String validEmail = "alice.brown@informatics.com";
        LocalDate validBirthDate = LocalDate.now().minusYears(25);

        Employee employee = new Employee(validName, validEmail, Gender.FEMALE, validBirthDate);

        assertNotNull(employee.getId(), "Every employee must be born with an initialized final UUID token");
        assertEquals("Alice Brown", employee.getName());
        assertEquals("alice.brown@informatics.com", employee.getEmail());
        assertEquals(Gender.FEMALE, employee.getGender());
        assertEquals(validBirthDate, employee.getBirthDate());
    }

    @Test
    void testConstructor_shouldThrowIllegalArgumentException_whenNameIsShorterThanTwoCharacters() {
        String corruptShortName = "A";
        String validEmail = "valid@informatics.com";
        LocalDate validBirthDate = LocalDate.now().minusYears(30);

        assertThrows(IllegalArgumentException.class, () ->
                new Employee(corruptShortName, validEmail, Gender.MALE, validBirthDate)
        );
    }

    @Test
    void testConstructor_shouldThrowAgeBoundaryException_whenApplicantIsUnderage() {
        String name = "Underage Worker";
        String email = "underage@informatics.com";
        LocalDate underageBirthDate = LocalDate.now().minusYears(16);

        assertThrows(AgeBoundaryException.class, () ->
                new Employee(name, email, Gender.MALE, underageBirthDate)
        );
    }

    @Test
    void testConstructor_shouldThrowIllegalArgumentException_whenEmailFormatIsCorrupt() {
        String name = "Valid Name";
        String corruptEmail = "invalid_email_missing_at_symbol.com";
        LocalDate validBirthDate = LocalDate.now().minusYears(28);

        assertThrows(IllegalArgumentException.class, () ->
                new Employee(name, corruptEmail, Gender.MALE, validBirthDate)
        );
    }
}
