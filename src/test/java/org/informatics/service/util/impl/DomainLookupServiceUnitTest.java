package org.informatics.service.util.impl;

import org.informatics.data.Company;
import org.informatics.data.Contract;
import org.informatics.data.Employee;
import org.informatics.data.Team;
import org.informatics.exceptions.EntityNotFoundException;
import org.informatics.service.util.DomainLookupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>Executes pure London-school unit tests targeting the DomainLookupService implementation module.</p>
 * <p>Mocks all collaborator entities to eliminate dependency cascades and analyze query streams
 * in absolute architectural isolation.</p>
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

        Employee mockEmployee = Mockito.mock(Employee.class);
        Mockito.when(mockEmployee.getId()).thenReturn(targetId);
        Mockito.when(mockEmployee.getName()).thenReturn("John Brooks");

        Contract mockContract = Mockito.mock(Contract.class);
        Mockito.when(mockContract.getEmployee()).thenReturn(mockEmployee);

        Set<Contract> contracts = new HashSet<>();
        contracts.add(mockContract);
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

        Employee mockEmployee = Mockito.mock(Employee.class);
        Mockito.when(mockEmployee.getId()).thenReturn(targetId);

        Contract mockContract = Mockito.mock(Contract.class);
        Mockito.when(mockContract.getContractNumber()).thenReturn(42);
        Mockito.when(mockContract.getEmployee()).thenReturn(mockEmployee);

        Set<Contract> contracts = new HashSet<>();
        contracts.add(mockContract);
        Mockito.when(company.getContracts()).thenReturn(contracts);

        // when
        Contract result = lookupService.findContractByEmployeeId(company, targetId);

        // then
        assertNotNull(result, "Should successfully return a valid Contract object wrapper");
        assertEquals(42, result.getContractNumber(), "The returned contract wrapper number must match the recorded integer key");
        assertEquals(mockEmployee, result.getEmployee(), "The nested employee profile inside the contract must match our target query employee");
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

        Employee mockManager = Mockito.mock(Employee.class);
        Contract mockManagerContract = Mockito.mock(Contract.class);
        Mockito.when(mockManagerContract.getEmployee()).thenReturn(mockManager);

        Team mockTeam = Mockito.mock(Team.class);
        Mockito.when(mockTeam.getId()).thenReturn(targetTeamId);
        Mockito.when(mockTeam.getManagerContract()).thenReturn(mockManagerContract);

        Set<Team> teams = new HashSet<>();
        teams.add(mockTeam);
        Mockito.when(company.getTeams()).thenReturn(teams);

        // when
        Team result = lookupService.findTeamById(company, targetTeamId);

        // then
        assertNotNull(result, "Should successfully return a valid Team tracking block");
        assertEquals(targetTeamId, result.getId(), "The structural ID of the returned team must match the query parameter token");
        assertEquals(mockManager, result.getManagerContract().getEmployee(), "The manager object bound to the team must match the original reference");
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
