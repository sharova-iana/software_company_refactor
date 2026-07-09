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
        String email = "jane.doe@informatics.com";
        Position position = Position.UI_UX_DESIGNER;
        BigDecimal salary = new BigDecimal("3000.00");

        java.util.Map<Position, BigDecimal> emptySalariesMap = new java.util.EnumMap<>(Position.class);
        Mockito.when(mockCompany.getPositionMinimumSalaries()).thenReturn(emptySalariesMap);

        // when/then
        assertThrows(SalaryConfigurationException.class, () -> {
            employeeService.hireEmployee(mockCompany, name, email, Gender.FEMALE, LocalDate.now().minusYears(25), position, salary);
        }, "Cannot hire. No entry-level salary baseline configured for position: UI_UX_DESIGNER");

        Mockito.verify(mockCompany).getPositionMinimumSalaries();
    }

    @Test
    void testHireEmployee_shouldThrowInvalidSalaryException_whenSalaryIsBelowMinimumFloor() {
        // given
        String name = "Underpaid Dev";
        String email = "underpaid@informatics.com";
        Position position = Position.SENIOR_DEVELOPER;
        BigDecimal substandardSalary = new BigDecimal("5500.00");

        java.util.Map<Position, BigDecimal> mockSalariesMap = new java.util.EnumMap<>(Position.class);
        mockSalariesMap.put(Position.SENIOR_DEVELOPER, new BigDecimal("6000.00"));
        Mockito.when(mockCompany.getPositionMinimumSalaries()).thenReturn(mockSalariesMap);

        // when/then
        assertThrows(InvalidSalaryException.class, () -> {
            employeeService.hireEmployee(mockCompany, name, email, Gender.MALE, LocalDate.now().minusYears(35), position, substandardSalary);
        });

        Mockito.verify(mockCompany).getPositionMinimumSalaries();
    }

    @Test
    void testHireEmployee_shouldInstantiateContractAndCallCompanyAddContract_whenArgumentsAreValid() {
        // given
        String name = "Jack Doe";
        String email = "jack.doe@informatics.com";
        LocalDate birthDate = LocalDate.now().minusYears(30);
        BigDecimal negotiatedSalary = new BigDecimal("6500.00");

        java.util.Map<Position, BigDecimal> mockSalariesMap = new java.util.EnumMap<>(Position.class);
        mockSalariesMap.put(Position.SENIOR_DEVELOPER, new BigDecimal("6000.00"));
        Mockito.when(mockCompany.getPositionMinimumSalaries()).thenReturn(mockSalariesMap);

        Mockito.when(mockCompany.incrementAndGetContractCounter()).thenReturn(101);

        // when: hiring now yields the active legal Contract object back to our test layer
        Contract actualContract = employeeService.hireEmployee(mockCompany, name, email, Gender.MALE, birthDate, Position.SENIOR_DEVELOPER, negotiatedSalary);

        // then
        assertNotNull(actualContract);
        assertEquals(Position.SENIOR_DEVELOPER, actualContract.getPosition());
        assertEquals(0, negotiatedSalary.compareTo(actualContract.getSalary()));
        assertEquals("Jack Doe", actualContract.getEmployee().getName());
        assertEquals("jack.doe@informatics.com", actualContract.getEmployee().getEmail());

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
        Mockito.when(mockManager.getName()).thenReturn("Stephen Manager");
        Mockito.when(mockManager.getEmail()).thenReturn("stephen@informatics.com");

        Contract mockContract = Mockito.mock(Contract.class);
        Mockito.when(mockContract.getEmployee()).thenReturn(mockManager);

        Mockito.when(mockContract.getPosition()).thenReturn(Position.MANAGER);
        Mockito.when(mockContract.getContractNumber()).thenReturn(401);

        Team mockTeam = Mockito.mock(Team.class);
        Mockito.when(mockTeam.getManagerContract()).thenReturn(mockContract);

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
    void testFireEmployee_shouldPurgeContractAndCallTeamRemoveMemberContract_whenEmployeeIsARegularContributor() {
        // given
        UUID targetDevId = UUID.randomUUID();

        Employee mockDeveloper = Mockito.mock(Employee.class);
        Mockito.when(mockDeveloper.getName()).thenReturn("John Brooks");
        Mockito.when(mockDeveloper.getEmail()).thenReturn("john.brooks@informatics.com");

        Contract mockContract = Mockito.mock(Contract.class);
        Mockito.when(mockContract.getEmployee()).thenReturn(mockDeveloper);
        Mockito.when(mockContract.getPosition()).thenReturn(Position.JUNIOR_DEVELOPER);
        Mockito.when(mockContract.getContractNumber()).thenReturn(402);

        Team mockTeam = Mockito.mock(Team.class);
        java.util.Set<Contract> mockMemberContractsSet = new java.util.HashSet<>();
        mockMemberContractsSet.add(mockContract);
        Mockito.when(mockTeam.getMemberContracts()).thenReturn(mockMemberContractsSet);

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
        Mockito.verify(mockTeam).removeMemberContract(mockContract);
    }
}
