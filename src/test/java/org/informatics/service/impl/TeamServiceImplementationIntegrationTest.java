package org.informatics.service.impl;

import org.informatics.data.Company;
import org.informatics.data.Contract;
import org.informatics.data.Employee;
import org.informatics.data.Team;
import org.informatics.data.enums.Gender;
import org.informatics.data.enums.Position;
import org.informatics.exceptions.TeamAssignmentException;
import org.informatics.service.TeamService;
import org.informatics.service.util.DomainLookupService;
import org.informatics.service.util.impl.DomainLookupServiceImplementation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Executes domain-level integration tests for the TeamService module.
 * Verifies live group coordination constraints and structural business policy guards.
 */
class TeamServiceImplementationIntegrationTest {

    private TeamService teamService;
    private Company company;
    private Employee manager;
    private Employee developer;

    @BeforeEach
    void setUp() {
        DomainLookupService lookupService = new DomainLookupServiceImplementation();
        EmployeeServiceImplementation emplService = new EmployeeServiceImplementation(lookupService);
        teamService = new TeamServiceImplementation(lookupService);
        company = new Company("Team Integration Corp");

        company.setSalaryForPosition(Position.MANAGER, new BigDecimal("5000.00"));
        company.setSalaryForPosition(Position.JUNIOR_DEVELOPER, new BigDecimal("2000.00"));

        manager = emplService.hireEmployee(company, "Jack Doe", Gender.MALE, LocalDate.now().minusYears(45), Position.MANAGER, new BigDecimal("5500.00"));
        developer = emplService.hireEmployee(company, "Helen Smith", Gender.FEMALE, LocalDate.now().minusYears(23), Position.JUNIOR_DEVELOPER, new BigDecimal("2400.00"));
    }

    // =========================================================================
    // METHOD UNDER TEST: createTeam
    // =========================================================================

    @Test
    void testCreateTeam_shouldRegisterSuccessfully_whenManagerIsAvailable() {
        // when
        Team team = teamService.createTeam(company, manager.getId());

        // then
        assertNotNull(team);
        assertEquals(manager, team.getManager());
        assertTrue(company.getTeams().contains(team));
    }

    @Test
    void testCreateTeam_shouldThrowTeamAssignmentException_whenManagerAlreadyLeadsAnotherTeam() {
        // given: Build an initial team block for this manager reference
        teamService.createTeam(company, manager.getId());

        // when/then: Second team attempt under identical leader ID must hit cross-entity rule guards
        assertThrows(TeamAssignmentException.class, () ->
                teamService.createTeam(company, manager.getId())
        );
    }

    // =========================================================================
    // METHOD UNDER TEST: addMemberToTeam
    // =========================================================================

    @Test
    void testAddMemberToTeam_shouldAppendContributor_whenBusinessRulesPass() {
        // given
        Team team = teamService.createTeam(company, manager.getId());

        // when
        boolean result = teamService.addMemberToTeam(company, team.getId(), developer.getId());

        // then
        assertTrue(result);
        assertTrue(team.getMembers().contains(developer));
    }

    @Test
    void testAddMemberToTeam_shouldThrowTeamAssignmentException_whenEmployeeIsAlreadyInAnotherTeam() {
        // given
        Team team1 = teamService.createTeam(company, manager.getId());
        teamService.addMemberToTeam(company, team1.getId(), developer.getId());

        // Hire an alternate manager to establish a second team pipeline
        Employee manager2 = new Employee("Second Leader", Gender.FEMALE, LocalDate.now().minusYears(35), Position.MANAGER, new BigDecimal("5000"));
        company.addContract(new Contract(99, manager2));
        Team team2 = teamService.createTeam(company, manager2.getId());

        // when/then: Appending a developer active elsewhere must fail
        assertThrows(TeamAssignmentException.class, () ->
                teamService.addMemberToTeam(company, team2.getId(), developer.getId())
        );
    }

    // =========================================================================
    // METHOD UNDER TEST: removeMemberFromTeam
    // =========================================================================

    @Test
    void testRemoveMemberFromTeam_shouldEvictContributor_whenActiveInPool() {
        // given
        Team team = teamService.createTeam(company, manager.getId());
        teamService.addMemberToTeam(company, team.getId(), developer.getId());

        // when
        boolean result = teamService.removeMemberFromTeam(company, developer.getId());

        // then
        assertTrue(result);
        assertFalse(team.getMembers().contains(developer));
    }
}
