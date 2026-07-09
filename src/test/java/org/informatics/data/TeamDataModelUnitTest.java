package org.informatics.data;

import org.informatics.data.enums.Position;
import org.informatics.exceptions.PositionMismatchException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;


class TeamDataModelUnitTest {

    // =========================================================================
    // METHOD UNDER TEST: Team (Constructor)
    // =========================================================================

    @Test
    void testConstructor_shouldInstantiateCorrectly_whenLeaderHoldsManagerRole() {
        // given
        Employee mockEmployee = Mockito.mock(Employee.class);
        Mockito.when(mockEmployee.getName()).thenReturn("Jacob Black");

        Contract mockManagerContract = Mockito.mock(Contract.class);
        Mockito.when(mockManagerContract.getPosition()).thenReturn(Position.MANAGER);
        Mockito.when(mockManagerContract.getEmployee()).thenReturn(mockEmployee);

        // when
        Team team = new Team(mockManagerContract);

        // then
        assertNotNull(team.getId());
        assertEquals(mockManagerContract, team.getManagerContract());
        assertTrue(team.getMemberContracts().isEmpty(), "Fresh corporate team structures must be initialized with an empty membership set.");
    }

    @Test
    void testConstructor_shouldThrowPositionMismatchException_whenLeaderIsADeveloper() {
        // given
        Employee mockEmployee = Mockito.mock(Employee.class);
        Mockito.when(mockEmployee.getName()).thenReturn("Jack Doe");

        Contract mockDeveloperContract = Mockito.mock(Contract.class);
        Mockito.when(mockDeveloperContract.getPosition()).thenReturn(Position.JUNIOR_DEVELOPER);
        Mockito.when(mockDeveloperContract.getEmployee()).thenReturn(mockEmployee);

        // when/then: The team aggregate shield must catch the mock role and fail fast
        assertThrows(PositionMismatchException.class, () -> new Team(mockDeveloperContract),
                "The constructor domain aggregate shield must reject non-manager contract roles from leading team shells.");
    }
}
