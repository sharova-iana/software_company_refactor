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
 * Executes domain-level sociable integration tests for the DomainLookupService module.
 * Verifies character streams and lookup paths against real, active domain objects in memory without mocking.
 */
class DomainLookupServiceImplementationIntegrationTest {

    private DomainLookupService lookupService;
    private Company company;
    private Employee manager;
    private Employee developer;
    private Contract managerContract;
    private Team team;

    @BeforeEach
    void setUp() {
        lookupService = new DomainLookupServiceImplementation();
        company = new Company("Lookup Integration Labs");

        // 1. Initialize real domain objects
        manager = new Employee("Joe Smith", Gender.MALE, LocalDate.of(1984, 5, 15), Position.MANAGER, new BigDecimal("6000.00"));
        developer = new Employee("Sarah Brown", Gender.FEMALE, LocalDate.of(1996, 11, 20), Position.JUNIOR_DEVELOPER, new BigDecimal("2800.00"));

        managerContract = new Contract(101, manager);
        Contract devContract = new Contract(102, developer);

        team = new Team(manager);
        team.addMember(developer);

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
        assertEquals(developer, result);
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
        assertEquals(managerContract, resultContract);
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
        assertTrue(resultTeam.getMembers().contains(developer));
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
