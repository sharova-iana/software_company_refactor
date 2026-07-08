package org.informatics.service.impl;

import org.informatics.data.Company;
import org.informatics.data.Contract;
import org.informatics.data.Employee;
import org.informatics.data.Team;
import org.informatics.data.enums.Gender;
import org.informatics.data.enums.Position;
import org.informatics.exceptions.InvalidSalaryException;
import org.informatics.exceptions.SalaryConfigurationException;
import org.informatics.service.util.DomainLookupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Executes pure, strict London-school unit tests targeting the EmployeeService implementation module.
 * Following Mockist standards, every collaborator and rich data aggregate dependency is completely mocked,
 * ensuring business logic calculations are evaluated in absolute structural isolation.
 */
class EmployeeServiceImplementationUnitTest {

    private EmployeeServiceImplementation employeeService;
    private DomainLookupService mockLookupService;
    private Company mockCompany;

    @BeforeEach
    void setUp() {
        mockLookupService = Mockito.mock(DomainLookupService.class);
        employeeService = new EmployeeServiceImplementation(mockLookupService);
        mockCompany = Mockito.mock(Company.class);
    }

    // =========================================================================
    // METHOD UNDER TEST: hireEmployee
    // =========================================================================

    @Test
    void testHireEmployee_shouldThrowSalaryConfigurationException_whenPositionHasNoConfiguredFloor() {
        // given
        String name = "Jane Doe";
        Position position = Position.UI_UX_DESIGNER;
        BigDecimal salary = new BigDecimal("3000.00");

        java.util.Map<Position, BigDecimal> emptySalariesMap = new java.util.EnumMap<>(Position.class);
        Mockito.when(mockCompany.getPositionMinimumSalaries()).thenReturn(emptySalariesMap);

        // when/then
        assertThrows(SalaryConfigurationException.class, () -> {
            employeeService.hireEmployee(mockCompany, name, Gender.FEMALE, LocalDate.now().minusYears(25), position, salary);
        }, "Cannot hire. No entry-level salary baseline configured for position: UI_UX_DESIGNER");

        Mockito.verify(mockCompany).getPositionMinimumSalaries();
    }

    @Test
    void testHireEmployee_shouldThrowInvalidSalaryException_whenSalaryIsBelowMinimumFloor() {
        // given
        String name = "Underpaid Dev";
        Position position = Position.SENIOR_DEVELOPER;
        BigDecimal substandardSalary = new BigDecimal("5500.00");

        java.util.Map<Position, BigDecimal> mockSalariesMap = new java.util.EnumMap<>(Position.class);
        mockSalariesMap.put(Position.SENIOR_DEVELOPER, new BigDecimal("6000.00"));
        Mockito.when(mockCompany.getPositionMinimumSalaries()).thenReturn(mockSalariesMap);

        // when/then
        assertThrows(InvalidSalaryException.class, () -> {
            employeeService.hireEmployee(mockCompany, name, Gender.MALE, LocalDate.now().minusYears(35), position, substandardSalary);
        });

        Mockito.verify(mockCompany).getPositionMinimumSalaries();
    }

    @Test
    void testHireEmployee_shouldInstantiateEmployeeAndCallCompanyAddContract_whenArgumentsAreValid() {
        // given
        String name = "Jack Doe";
        LocalDate birthDate = LocalDate.now().minusYears(30);
        BigDecimal negotiatedSalary = new BigDecimal("6500.00");

        java.util.Map<Position, BigDecimal> mockSalariesMap = new java.util.EnumMap<>(Position.class);
        mockSalariesMap.put(Position.SENIOR_DEVELOPER, new BigDecimal("6000.00"));
        Mockito.when(mockCompany.getPositionMinimumSalaries()).thenReturn(mockSalariesMap);

        Mockito.when(mockCompany.incrementAndGetContractCounter()).thenReturn(101);

        // when
        Employee actualEmployee = employeeService.hireEmployee(mockCompany, name, Gender.MALE, birthDate, Position.SENIOR_DEVELOPER, negotiatedSalary);

        // then
        assertNotNull(actualEmployee);
        assertEquals("Jack Doe", actualEmployee.getName());

        // Verify that service issued the correct mutation command to the aggregate model
        Mockito.verify(mockCompany).incrementAndGetContractCounter();
        Mockito.verify(mockCompany).addContract(Mockito.any(Contract.class));
    }

    // =========================================================================
    // METHOD UNDER TEST: fireEmployee
    // =========================================================================

    @Test
    void testFireEmployee_shouldPurgeContractAndCallCompanyRemoveTeam_whenEmployeeIsAManager() {
        // given
        UUID targetManagerId = UUID.randomUUID();

        Employee mockManager = Mockito.mock(Employee.class);
        Mockito.when(mockManager.getPosition()).thenReturn(Position.MANAGER);

        Contract mockContract = Mockito.mock(Contract.class);
        Mockito.when(mockContract.getEmployee()).thenReturn(mockManager);

        Team mockTeam = Mockito.mock(Team.class);
        Mockito.when(mockTeam.getManager()).thenReturn(mockManager);

        java.util.Set<Team> mockTeamsSet = new java.util.HashSet<>();
        mockTeamsSet.add(mockTeam);
        Mockito.when(mockCompany.getTeams()).thenReturn(mockTeamsSet);

        Mockito.when(mockLookupService.findContractByEmployeeId(mockCompany, targetManagerId)).thenReturn(mockContract);

        // when
        boolean result = employeeService.fireEmployee(mockCompany, targetManagerId);

        // then
        assertTrue(result);

        Mockito.verify(mockLookupService).findContractByEmployeeId(mockCompany, targetManagerId);
        Mockito.verify(mockCompany).removeContract(mockContract);
        Mockito.verify(mockCompany).removeTeam(mockTeam);
    }

    @Test
    void testFireEmployee_shouldPurgeContractAndCallTeamRemoveMember_whenEmployeeIsARegularContributor() {
        // given
        UUID targetDevId = UUID.randomUUID();

        Employee mockDeveloper = Mockito.mock(Employee.class);
        Mockito.when(mockDeveloper.getPosition()).thenReturn(Position.JUNIOR_DEVELOPER);

        Contract mockContract = Mockito.mock(Contract.class);
        Mockito.when(mockContract.getEmployee()).thenReturn(mockDeveloper);

        Team mockTeam = Mockito.mock(Team.class);
        java.util.Set<Employee> mockMembersSet = new java.util.HashSet<>();
        mockMembersSet.add(mockDeveloper);
        Mockito.when(mockTeam.getMembers()).thenReturn(mockMembersSet);

        java.util.Set<Team> mockTeamsSet = new java.util.HashSet<>();
        mockTeamsSet.add(mockTeam);
        Mockito.when(mockCompany.getTeams()).thenReturn(mockTeamsSet);

        Mockito.when(mockLookupService.findContractByEmployeeId(mockCompany, targetDevId)).thenReturn(mockContract);

        // when
        boolean result = employeeService.fireEmployee(mockCompany, targetDevId);

        // then
        assertTrue(result);

        Mockito.verify(mockLookupService).findContractByEmployeeId(mockCompany, targetDevId);
        Mockito.verify(mockCompany).removeContract(mockContract);
        Mockito.verify(mockTeam).removeMember(mockDeveloper);
    }
}
