package org.informatics.service.util.impl;

import org.informatics.data.Company;
import org.informatics.data.Contract;
import org.informatics.data.Employee;
import org.informatics.data.Team;
import org.informatics.data.enums.Gender;
import org.informatics.data.enums.Position;
import org.informatics.exceptions.EntityNotFoundException;
import org.informatics.service.util.DomainLookupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>Executes domain-level sociable integration tests for the DomainLookupService module.</p>
 * <p>Verifies object streams and lookup paths against real, active domain objects in memory
 * without mocking, capturing true cross-entity integration behavior.</p>
 */
class DomainLookupServiceImplementationIntegrationTest {

    private DomainLookupService lookupService;
    private Company company;
    private Employee manager;
    private Employee developer;
    private Contract managerContract;
    private Contract devContract;
    private Team team;

    @BeforeEach
    void setUp() {
        lookupService = new DomainLookupServiceImplementation();
        company = new Company("Lookup Integration Labs");

        manager = new Employee("Joe Smith", "joe.smith@informatics.com", Gender.MALE, LocalDate.of(1984, 5, 15));
        developer = new Employee("Sarah Brown", "sarah.brown@informatics.com", Gender.FEMALE, LocalDate.of(1996, 11, 20));

        managerContract = new Contract(101, manager, Position.MANAGER, new BigDecimal("6000.00"));
        devContract = new Contract(102, developer, Position.JUNIOR_DEVELOPER, new BigDecimal("2800.00"));

        team = new Team(managerContract);
        team.addMemberContract(devContract);

        company.addContract(managerContract);
        company.addContract(devContract);
        company.addTeam(team);
    }

    // =========================================================================
    // METHOD UNDER TEST: findEmployeeById
    // =========================================================================

    @Test
    void testFindEmployeeById_shouldReturnCorrectEmployee_whenIdIsActiveInCompany() {
        // when
        Employee result = lookupService.findEmployeeById(company, developer.getId());

        // then
        assertNotNull(result);
        assertEquals(developer, result, "The lookup engine must traverse the contract list to discover the matching human identity reference.");
    }

    @Test
    void testFindEmployeeById_shouldThrowEntityNotFoundException_whenIdIsMissing() {
        // given
        UUID nonExistentEmployeeId = UUID.randomUUID();

        // when/then
        assertThrows(EntityNotFoundException.class, () ->
                lookupService.findEmployeeById(company, nonExistentEmployeeId)
        );
    }

    // =========================================================================
    // METHOD UNDER TEST: findContractByEmployeeId
    // =========================================================================

    @Test
    void testFindContractByEmployeeId_shouldReturnCorrectContractWrapper_whenEmployeeIdIsActive() {
        // when
        Contract resultContract = lookupService.findContractByEmployeeId(company, manager.getId());

        // then
        assertNotNull(resultContract);
        assertEquals(managerContract, resultContract, "The lookup engine must identify the correct active legal contract wrapping the human target ID.");
    }

    @Test
    void testFindContractByEmployeeId_shouldThrowEntityNotFoundException_whenEmployeeIdIsMissing() {
        // given
        UUID nonExistentEmployeeId = UUID.randomUUID();

        // when/then
        assertThrows(EntityNotFoundException.class, () ->
                lookupService.findContractByEmployeeId(company, nonExistentEmployeeId)
        );
    }

    // =========================================================================
    // METHOD UNDER TEST: findTeamById
    // =========================================================================

    @Test
    void testFindTeamById_shouldReturnCorrectTeam_whenTeamIdIsActiveInCompany() {
        // when
        Team resultTeam = lookupService.findTeamById(company, team.getId());

        // then
        assertNotNull(resultTeam);
        assertEquals(team, resultTeam);

        assertTrue(resultTeam.getMemberContracts().contains(devContract), "The retrieved team aggregate must preserve its nested legal contract member associations.");
    }

    @Test
    void testFindTeamById_shouldThrowEntityNotFoundException_whenTeamIdIsMissing() {
        // given
        UUID nonExistentTeamId = UUID.randomUUID();

        // when/then
        assertThrows(EntityNotFoundException.class, () ->
                lookupService.findTeamById(company, nonExistentTeamId)
        );
    }
}
