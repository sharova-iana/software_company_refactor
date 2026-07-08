package org.informatics.service.impl;

import org.informatics.data.Company;
import org.informatics.data.Employee;
import org.informatics.data.Team;
import org.informatics.data.enums.Position;
import org.informatics.exceptions.TeamAssignmentException;
import org.informatics.service.util.DomainLookupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Executes pure, strict London-school unit tests targeting the TeamService implementation module.
 * Following Mockist standards, every collaborator and rich domain aggregate entity is completely mocked,
 * isolating structural business adjustments under complete memory protection.
 */
class TeamServiceImplementationUnitTest {

    private TeamServiceImplementation teamService;
    private DomainLookupService mockLookupService;
    private Company mockCompany;

    @BeforeEach
    void setUp() {
        mockLookupService = Mockito.mock(DomainLookupService.class);
        teamService = new TeamServiceImplementation(mockLookupService);
        mockCompany = Mockito.mock(Company.class);
    }

    // =========================================================================
    // METHOD UNDER TEST: createTeam
    // =========================================================================

    @Test
    void testCreateTeam_shouldThrowTeamAssignmentException_whenManagerIsAlreadyLeadingAnotherTeam() {
        // given
        UUID managerId = UUID.randomUUID();
        Employee mockManager = Mockito.mock(Employee.class);

        // Stub the lookup utility to return our manager reference
        Mockito.when(mockLookupService.findEmployeeById(mockCompany, managerId)).thenReturn(mockManager);

        // Set up a pre-existing team led by this exact same manager to force a policy collision
        Team preExistingTeam = Mockito.mock(Team.class);
        Mockito.when(preExistingTeam.getManager()).thenReturn(mockManager);

        Set<Team> activeTeams = new HashSet<>();
        activeTeams.add(preExistingTeam);
        Mockito.when(mockCompany.getTeams()).thenReturn(activeTeams);

        // when/then
        assertThrows(TeamAssignmentException.class, () -> {
            teamService.createTeam(mockCompany, managerId);
        }, "Should throw an exception if the selected manager is already managing another corporate team shell");

        Mockito.verify(mockLookupService).findEmployeeById(mockCompany, managerId);
        Mockito.verify(mockCompany).getTeams();
    }

    @Test
    void testCreateTeam_shouldInstantiateTeamAndCallCompanyAddTeam_whenArgumentsAreValid() {
        // given
        UUID managerId = UUID.randomUUID();
        Employee mockManager = Mockito.mock(Employee.class);
        Mockito.when(mockManager.getPosition()).thenReturn(Position.MANAGER);
        Mockito.when(mockManager.getName()).thenReturn("Valid Leader");

        Mockito.when(mockLookupService.findEmployeeById(mockCompany, managerId)).thenReturn(mockManager);

        // Return an empty teams registry to clear the single-team rule check
        Set<Team> emptyTeams = new HashSet<>();
        Mockito.when(mockCompany.getTeams()).thenReturn(emptyTeams);

        // when
        Team resultTeam = teamService.createTeam(mockCompany, managerId);

        // then
        assertNotNull(resultTeam, "The service orchestrator must successfully yield an instantiated team module reference");

        Mockito.verify(mockLookupService).findEmployeeById(mockCompany, managerId);
        Mockito.verify(mockCompany).getTeams();
        Mockito.verify(mockCompany).addTeam(Mockito.any(Team.class));
    }

    // =========================================================================
    // METHOD UNDER TEST: addMemberToTeam
    // =========================================================================

    @Test
    void testAddMemberToTeam_shouldThrowTeamAssignmentException_whenCandidateHoldsManagerRole() {
        // given
        UUID teamId = UUID.randomUUID();
        UUID candidateId = UUID.randomUUID();

        Team mockTeam = Mockito.mock(Team.class);
        Employee mockEmployee = Mockito.mock(Employee.class);
        Mockito.when(mockEmployee.getPosition()).thenReturn(Position.MANAGER);
        Mockito.when(mockEmployee.getName()).thenReturn("Accidental Contributor");

        Mockito.when(mockLookupService.findTeamById(mockCompany, teamId)).thenReturn(mockTeam);
        Mockito.when(mockLookupService.findEmployeeById(mockCompany, candidateId)).thenReturn(mockEmployee);

        // when/then
        assertThrows(TeamAssignmentException.class, () -> {
            teamService.addMemberToTeam(mockCompany, teamId, candidateId);
        }, "Should reject assignment immediately if a manager attempts to join a contributor member set");
    }
    @Test
    void testAddMemberToTeam_shouldThrowTeamAssignmentException_whenEmployeeIsAlreadyAssignedToAnotherTeam() {
        // given
        UUID teamId = UUID.randomUUID();
        UUID candidateId = UUID.randomUUID();

        Team mockTargetTeam = Mockito.mock(Team.class);
        Employee mockEmployee = Mockito.mock(Employee.class);
        Mockito.when(mockEmployee.getPosition()).thenReturn(Position.JUNIOR_DEVELOPER);

        Mockito.when(mockLookupService.findTeamById(mockCompany, teamId)).thenReturn(mockTargetTeam);
        Mockito.when(mockLookupService.findEmployeeById(mockCompany, candidateId)).thenReturn(mockEmployee);

        // Create an alternative team that already encloses this employee inside its membership list view
        Team alternativeTeam = Mockito.mock(Team.class);
        Set<Employee> alternativeMembers = new HashSet<>();
        alternativeMembers.add(mockEmployee);
        Mockito.when(alternativeTeam.getMembers()).thenReturn(alternativeMembers);

        Set<Team> activeCompanyTeams = new HashSet<>();
        activeCompanyTeams.add(alternativeTeam);
        Mockito.when(mockCompany.getTeams()).thenReturn(activeCompanyTeams);

        // when/then
        assertThrows(TeamAssignmentException.class, () -> {
            teamService.addMemberToTeam(mockCompany, teamId, candidateId);
        }, "Should reject assignment if an employee violates single-team assignment rules");
    }

    @Test
    void testAddMemberToTeam_shouldCallTeamAddMember_whenCandidatePassesAllBusinessRules() {
        // given
        UUID teamId = UUID.randomUUID();
        UUID candidateId = UUID.randomUUID();

        Team mockTargetTeam = Mockito.mock(Team.class);
        Employee mockEmployee = Mockito.mock(Employee.class);
        Mockito.when(mockEmployee.getPosition()).thenReturn(Position.JUNIOR_DEVELOPER);

        Mockito.when(mockLookupService.findTeamById(mockCompany, teamId)).thenReturn(mockTargetTeam);
        Mockito.when(mockLookupService.findEmployeeById(mockCompany, candidateId)).thenReturn(mockEmployee);

        Set<Team> activeCompanyTeams = new HashSet<>();
        activeCompanyTeams.add(mockTargetTeam);
        Mockito.when(mockCompany.getTeams()).thenReturn(activeCompanyTeams);

        Mockito.when(mockTargetTeam.getMembers()).thenReturn(new HashSet<>()); // Empty members means employee isn't a duplicate
        Mockito.when(mockTargetTeam.addMember(mockEmployee)).thenReturn(true);

        // when
        boolean result = teamService.addMemberToTeam(mockCompany, teamId, candidateId);

        // then
        assertTrue(result, "The service orchestrator must report successful membership append validation");

        // Verify the command reached our rich domain object mutator channel
        Mockito.verify(mockTargetTeam).addMember(mockEmployee);
    }


    // =========================================================================
    // METHOD UNDER TEST: dissolveTeam
    // =========================================================================

    @Test
    void testDissolveTeam_shouldCallCompanyRemoveTeam_whenTeamIdIsValid() {
        // given
        UUID targetTeamId = UUID.randomUUID();

        Employee mockLeader = Mockito.mock(Employee.class);
        Mockito.when(mockLeader.getName()).thenReturn("Leader Profile");

        Team mockTargetTeam = Mockito.mock(Team.class);
        Mockito.when(mockTargetTeam.getManager()).thenReturn(mockLeader);

        Mockito.when(mockLookupService.findTeamById(mockCompany, targetTeamId)).thenReturn(mockTargetTeam);
        Mockito.when(mockCompany.removeTeam(mockTargetTeam)).thenReturn(true);

        // when
        boolean result = teamService.dissolveTeam(mockCompany, targetTeamId);

        // then
        assertTrue(result);

        // Pure London Verification: Prove aggregate root was requested to drop this object directly
        Mockito.verify(mockLookupService).findTeamById(mockCompany, targetTeamId);
        Mockito.verify(mockCompany).removeTeam(mockTargetTeam);
    }

    // =========================================================================
    // METHOD UNDER TEST: removeMemberFromTeam
    // =========================================================================

    @Test
    void testRemoveMemberFromTeam_shouldThrowTeamAssignmentException_whenTargetIdBelongsToAManager() {
        // given
        UUID mockId = UUID.randomUUID();
        Employee mockManager = Mockito.mock(Employee.class);
        Mockito.when(mockManager.getPosition()).thenReturn(Position.MANAGER);
        Mockito.when(mockManager.getName()).thenReturn("Manager Object");

        Mockito.when(mockLookupService.findEmployeeById(mockCompany, mockId)).thenReturn(mockManager);

        // when/then
        assertThrows(TeamAssignmentException.class, () -> {
            teamService.removeMemberFromTeam(mockCompany, mockId);
        }, "Should throw an exception if the user tries to evict a manager from contributor-level views");
    }

    @Test
    void testRemoveMemberFromTeam_shouldThrowTeamAssignmentException_whenEmployeeIsNotActiveInAnyTeam() {
        // given
        UUID targetDevId = UUID.randomUUID();
        Employee mockDeveloper = Mockito.mock(Employee.class);
        Mockito.when(mockDeveloper.getPosition()).thenReturn(Position.JUNIOR_DEVELOPER);
        Mockito.when(mockDeveloper.getName()).thenReturn("Unassigned Dev");

        Mockito.when(mockLookupService.findEmployeeById(mockCompany, targetDevId)).thenReturn(mockDeveloper);

        // Return an empty collection view to force the internal dynamic team search to come up empty
        Set<Team> emptyTeamsSet = new HashSet<>();
        Mockito.when(mockCompany.getTeams()).thenReturn(emptyTeamsSet);

        // when/then
        assertThrows(TeamAssignmentException.class, () -> {
            teamService.removeMemberFromTeam(mockCompany, targetDevId);
        }, "Should throw an assignment exception if an employee is unassigned");
    }

    @Test
    void testRemoveMemberFromTeam_shouldCallTeamRemoveMember_whenEmployeeIsActiveInsideATeamPool() {
        // given
        UUID targetDevId = UUID.randomUUID();
        Employee mockDeveloper = Mockito.mock(Employee.class);
        Mockito.when(mockDeveloper.getPosition()).thenReturn(Position.JUNIOR_DEVELOPER);

        Mockito.when(mockLookupService.findEmployeeById(mockCompany, targetDevId)).thenReturn(mockDeveloper);

        Team mockAssignedTeam = Mockito.mock(Team.class);
        Set<Employee> teamMembers = new HashSet<>();
        teamMembers.add(mockDeveloper);

        Mockito.when(mockAssignedTeam.getMembers()).thenReturn(teamMembers);
        Mockito.when(mockAssignedTeam.removeMember(mockDeveloper)).thenReturn(true);

        Set<Team> companyTeamsList = new HashSet<>();
        companyTeamsList.add(mockAssignedTeam);
        Mockito.when(mockCompany.getTeams()).thenReturn(companyTeamsList);

        // when
        boolean result = teamService.removeMemberFromTeam(mockCompany, targetDevId);

        // then
        assertTrue(result);

        Mockito.verify(mockAssignedTeam).removeMember(mockDeveloper);
    }
}
