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
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Executes isolated London-school unit tests targeting the DomainLookupService implementation module.
 * Verifies contract lookups, personnel discoveries, and exception throwing boundaries for unmapped records.
 */
class DomainLookupServiceUnitTest {

    private DomainLookupService lookupService;
    private Company company;

    @BeforeEach
    void setUp() {
        lookupService = new DomainLookupServiceImplementation();
        company = Mockito.mock(Company.class);
    }

    // =========================================================================
    // METHOD UNDER TEST: findEmployeeById
    // =========================================================================

    @Test
    void testFindEmployeeById_shouldReturnEmployee_whenIdIsRegisteredInContracts() {
        // given
        UUID targetId = UUID.randomUUID();
        Employee employee = new Employee("John Brooks", Gender.MALE, LocalDate.of(1990, 5, 5), Position.SENIOR_DEVELOPER, new BigDecimal("6000.00"));

        try {
            java.lang.reflect.Field idField = Employee.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(employee, targetId);
        } catch (Exception e) { throw new RuntimeException(e); }

        Contract contract = new Contract(1, employee);
        Set<Contract> contracts = new HashSet<>();
        contracts.add(contract);
        Mockito.when(company.getContracts()).thenReturn(contracts);

        // when
        Employee result = lookupService.findEmployeeById(company, targetId);

        // then
        assertNotNull(result, "Should successfully return a valid Employee instance profile");
        assertEquals(targetId, result.getId(), "The returned employee ID must match the query target ID parameter");
        assertEquals("John Brooks", result.getName());
    }

    @Test
    void testFindEmployeeById_shouldThrowEntityNotFoundException_whenIdIsUnregistered() {
        // given
        UUID missingId = UUID.randomUUID();
        Set<Contract> emptyContractsSet = new HashSet<>();
        Mockito.when(company.getContracts()).thenReturn(emptyContractsSet);

        // when/then
        assertThrows(EntityNotFoundException.class, () -> {
            lookupService.findEmployeeById(company, missingId);
        }, "Should throw EntityNotFoundException if the employee tracking token is completely missing");
    }

    // =========================================================================
    // METHOD UNDER TEST: findContractByEmployeeId
    // =========================================================================

    @Test
    void testFindContractByEmployeeId_shouldReturnContract_whenEmployeeIdIsRegistered() {
        // given
        UUID targetId = UUID.randomUUID();
        Employee employee = new Employee("Jane Smith", Gender.FEMALE, LocalDate.of(1994, 2, 2), Position.QA_ENGINEER, new BigDecimal("4000.00"));

        try {
            java.lang.reflect.Field idField = Employee.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(employee, targetId);
        } catch (Exception e) { throw new RuntimeException(e); }

        Contract contract = new Contract(42, employee);
        Set<Contract> contracts = new HashSet<>();
        contracts.add(contract);
        Mockito.when(company.getContracts()).thenReturn(contracts);

        // when
        Contract result = lookupService.findContractByEmployeeId(company, targetId);

        // then
        assertNotNull(result, "Should successfully return a valid Contract object wrapper");
        assertEquals(42, result.getContractNumber(), "The returned contract wrapper number must match the recorded integer key");
        assertEquals(employee, result.getEmployee(), "The nested employee profile inside the contract must match our target query employee");
    }

    @Test
    void testFindContractByEmployeeId_shouldThrowEntityNotFoundException_whenEmployeeIdIsUnregistered() {
        // given
        UUID missingId = UUID.randomUUID();
        Set<Contract> emptyContractsSet = new HashSet<>();
        Mockito.when(company.getContracts()).thenReturn(emptyContractsSet);

        // when/then
        assertThrows(EntityNotFoundException.class, () -> {
            lookupService.findContractByEmployeeId(company, missingId);
        }, "No active employment contract found for Employee ID: " + missingId);
    }

    // =========================================================================
    // METHOD UNDER TEST: findTeamById
    // =========================================================================

    @Test
    void testFindTeamById_shouldReturnTeam_whenTeamIdIsRegistered() {
        // given
        UUID targetTeamId = UUID.randomUUID();
        Employee manager = new Employee("Team Leader", Gender.MALE, LocalDate.of(1988, 12, 12), Position.MANAGER, new BigDecimal("5500.00"));
        Team team = new Team(manager);

        try {
            java.lang.reflect.Field idField = Team.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(team, targetTeamId);
        } catch (Exception e) { throw new RuntimeException(e); }

        Set<Team> teams = new HashSet<>();
        teams.add(team);
        Mockito.when(company.getTeams()).thenReturn(teams);

        // when
        Team result = lookupService.findTeamById(company, targetTeamId);

        // then
        assertNotNull(result, "Should successfully return a valid Team tracking block");
        assertEquals(targetTeamId, result.getId(), "The structural ID of the returned team must match the query parameter token");
        assertEquals(manager, result.getManager(), "The manager object bound to the team must match the original group creator reference");
    }

    @Test
    void testFindTeamById_shouldThrowEntityNotFoundException_whenTeamIdIsUnregistered() {
        // given
        UUID missingTeamId = UUID.randomUUID();
        Set<Team> emptyTeamsSet = new HashSet<>();
        Mockito.when(company.getTeams()).thenReturn(emptyTeamsSet);

        // when/then
        assertThrows(EntityNotFoundException.class, () -> {
            lookupService.findTeamById(company, missingTeamId);
        }, "No team found with ID: " + missingTeamId);
    }
}
