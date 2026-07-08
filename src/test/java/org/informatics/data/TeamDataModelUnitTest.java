package org.informatics.data;

import org.informatics.data.Employee;
import org.informatics.data.Team;
import org.informatics.data.enums.Gender;
import org.informatics.data.enums.Position;
import org.informatics.exceptions.PositionMismatchException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Executes pure Detroit-style classicist unit tests for the Team data model invariants.
 * Verifies manager role enforcements, collection views, and secure modification pathways.
 */
class TeamDataModelUnitTest {

    // =========================================================================
    // METHOD UNDER TEST: Team (Constructor)
    // =========================================================================

    @Test
    void testConstructor_shouldInstantiateCorrectly_whenLeaderHoldsManagerRole() {
        // given
        Employee manager = new Employee("Jacob Black", Gender.MALE, LocalDate.now().minusYears(40), Position.MANAGER, new BigDecimal("6000"));

        // when
        Team team = new Team(manager);

        // then
        assertNotNull(team.getId());
        assertEquals(manager, team.getManager());
        assertTrue(team.getMembers().isEmpty(), "Fresh team instances must be initialized with an empty membership list");
    }

    @Test
    void testConstructor_shouldThrowPositionMismatchException_whenLeaderIsADeveloper() {
        // given
        Employee developer = new Employee("Jack Doe", Gender.MALE, LocalDate.now().minusYears(22), Position.JUNIOR_DEVELOPER, new BigDecimal("2500"));

        // when/then
        assertThrows(PositionMismatchException.class, () -> new Team(developer),
                "Constructor must protect domain invariants, blocking non-manager tiers from leading shells");
    }


}
