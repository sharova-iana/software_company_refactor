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
 * <p>Executes domain-level sociable integration tests for the TeamService module.</p>
 * <p>Verifies live group coordination constraints and structural contract business policy guards in memory.</p>
 */
class TeamServiceImplementationIntegrationTest {

    private TeamService teamService;
    private Company company;
    private Contract managerContract;
    private Contract devContract;

    @BeforeEach
    void setUp() {
        DomainLookupService lookupService = new DomainLookupServiceImplementation();
        EmployeeServiceImplementation emplService = new EmployeeServiceImplementation(lookupService);
        teamService = new TeamServiceImplementation(lookupService);
        company = new Company("Team Integration Corp");

        company.setSalaryForPosition(Position.MANAGER, new BigDecimal("5000.00"));
        company.setSalaryForPosition(Position.JUNIOR_DEVELOPER, new BigDecimal("2000.00"));

        // Updated initialization passing human communication email tokens and returning contract entities
        managerContract = emplService.hireEmployee(company, "Jack Doe", "jack.doe@integration.com", Gender.MALE, LocalDate.now().minusYears(45), Position.MANAGER, new BigDecimal("5500.00"));
        devContract = emplService.hireEmployee(company, "Helen Smith", "helen.smith@integration.com", Gender.FEMALE, LocalDate.now().minusYears(23), Position.JUNIOR_DEVELOPER, new BigDecimal("2400.00"));
    }

    // =========================================================================
    // METHOD UNDER TEST: createTeam
    // =========================================================================

    @Test
    void testCreateTeam_shouldRegisterSuccessfully_whenManagerIsAvailable() {
        // when
        Team team = teamService.createTeam(company, managerContract.getEmployee().getId());

        // then
        assertNotNull(team);
        assertEquals(managerContract, team.getManagerContract(), "The team must be associated with the active manager contract agreement.");
        assertTrue(company.getTeams().contains(team));
    }

    @Test
    void testCreateTeam_shouldThrowTeamAssignmentException_whenManagerAlreadyLeadsAnotherTeam() {
        // given: Build an initial team block for this manager contract reference
        teamService.createTeam(company, managerContract.getEmployee().getId());

        // when/then: Second team attempt under identical leader ID must hit cross-entity rule guards
        assertThrows(TeamAssignmentException.class, () ->
                teamService.createTeam(company, managerContract.getEmployee().getId())
        );
    }

    // =========================================================================
    // METHOD UNDER TEST: addMemberToTeam
    // =========================================================================

    @Test
    void testAddMemberToTeam_shouldAppendContributor_whenBusinessRulesPass() {
        // given
        Team team = teamService.createTeam(company, managerContract.getEmployee().getId());

        // when
        boolean result = teamService.addMemberToTeam(company, team.getId(), devContract.getEmployee().getId());

        // then
        assertTrue(result);
        assertTrue(team.getMemberContracts().contains(devContract), "The team contributor pool must contain the active developer contract.");
    }

    @Test
    void testAddMemberToTeam_shouldThrowTeamAssignmentException_whenEmployeeIsAlreadyInAnotherTeam() {
        // given
        Team team1 = teamService.createTeam(company, managerContract.getEmployee().getId());
        teamService.addMemberToTeam(company, team1.getId(), devContract.getEmployee().getId());

        // Hire an alternate manager to establish a second team pipeline
        Employee manager2 = new Employee("Second Leader", "second.leader@integration.com", Gender.FEMALE, LocalDate.now().minusYears(35));
        Contract contract2 = new Contract(99, manager2, Position.MANAGER, new BigDecimal("5000.00"));
        company.addContract(contract2);
        Team team2 = teamService.createTeam(company, manager2.getId());

        // when/then: Appending a developer contract active inside an alternate pool must fail
        assertThrows(TeamAssignmentException.class, () ->
                teamService.addMemberToTeam(company, team2.getId(), devContract.getEmployee().getId())
        );
    }

    @Test
    void testAddMemberToTeam_shouldThrowTeamAssignmentException_whenCandidateIsAManager() {
        // given
        Team team = teamService.createTeam(company, managerContract.getEmployee().getId());

        // when/then: Enforce policy guard that block any manager contract from assuming contributor assignments
        assertThrows(TeamAssignmentException.class, () ->
                        teamService.addMemberToTeam(company, team.getId(), managerContract.getEmployee().getId()),
                "Should throw TeamAssignmentException if a manager contract tries to sit in a regular contributor slot."
        );
    }
    // =========================================================================
    // METHOD UNDER TEST: dissolveTeam
    // =========================================================================

    @Test
    void testDissolveTeam_shouldPurgeTeamAggregate_whenTeamIdIsValid() {
        // given
        Team team = teamService.createTeam(company, managerContract.getEmployee().getId());
        assertTrue(company.getTeams().contains(team));

        // when
        boolean result = teamService.dissolveTeam(company, team.getId());

        // then
        assertTrue(result);
        assertFalse(company.getTeams().contains(team), "The team aggregate shell must be completely purged from company records upon dissolution.");
    }


    // =========================================================================
    // METHOD UNDER TEST: removeMemberFromTeam
    // =========================================================================

    @Test
    void testRemoveMemberFromTeam_shouldEvictContributor_whenActiveInPool() {
        // given
        Team team = teamService.createTeam(company, managerContract.getEmployee().getId());
        teamService.addMemberToTeam(company, team.getId(), devContract.getEmployee().getId());

        // when
        boolean result = teamService.removeMemberFromTeam(company, devContract.getEmployee().getId());

        // then
        assertTrue(result);
        assertFalse(team.getMemberContracts().contains(devContract), "The fired contributor contract must be dropped from the member contract ledger.");
    }

    @Test
    void testRemoveMemberFromTeam_shouldThrowTeamAssignmentException_whenTargetEmployeeIsAManager() {
        // when/then: A manager cannot be evicted from regular contributor pools; they must go through dissolveTeam
        assertThrows(TeamAssignmentException.class, () ->
                        teamService.removeMemberFromTeam(company, managerContract.getEmployee().getId()),
                "Eviction should fail fast if the requested employee token holds an active MANAGER contract configuration."
        );
    }

    @Test
    void testRemoveMemberFromTeam_shouldThrowTeamAssignmentException_whenEmployeeIsNoMemberOfAnyTeam() {
        // when/then: Evicting a worker who has never been assigned to any group pool must fail cleanly
        assertThrows(TeamAssignmentException.class, () ->
                        teamService.removeMemberFromTeam(company, devContract.getEmployee().getId()),
                "Eviction should fail fast if the contract worker is not currently linked inside any active team membership list."
        );
    }

}
