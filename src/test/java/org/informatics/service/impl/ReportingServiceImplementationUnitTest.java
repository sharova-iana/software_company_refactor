package org.informatics.service.impl;

import org.informatics.data.Company;
import org.informatics.data.Contract;
import org.informatics.data.Employee;
import org.informatics.data.Team;
import org.informatics.data.enums.Gender;
import org.informatics.data.enums.Position;
import org.informatics.service.ReportingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Executes pure, strict London-school unit tests targeting the ReportingService implementation module.
 * Following Mockist standards, every domain entity is completely mocked to verify tabular data frame
 * extractions in absolute structural isolation.
 */
class ReportingServiceImplementationUnitTest {

    private ReportingService reportingService;
    private Company mockCompany;

    @BeforeEach
    void setUp() {
        reportingService = new ReportingServiceImplementation();
        mockCompany = Mockito.mock(Company.class);
    }

    // =========================================================================
    // METHOD UNDER TEST: compileEmployeeTableData
    // =========================================================================

    @Test
    void testCompileEmployeeTableData_shouldReturnFormattedMatrixRows_whenContractsAreActive() {
        // given
        UUID empId = UUID.randomUUID();
        Employee mockEmployee = Mockito.mock(Employee.class);
        Mockito.when(mockEmployee.getId()).thenReturn(empId);
        Mockito.when(mockEmployee.getName()).thenReturn("John Brooks");
        Mockito.when(mockEmployee.getGender()).thenReturn(Gender.MALE);
        Mockito.when(mockEmployee.getBirthDate()).thenReturn(LocalDate.of(1992, 4, 15));

        Contract mockContract = Mockito.mock(Contract.class);
        Mockito.when(mockContract.getContractNumber()).thenReturn(101);
        Mockito.when(mockContract.getEmployee()).thenReturn(mockEmployee);
        Mockito.when(mockContract.getPosition()).thenReturn(Position.SENIOR_DEVELOPER);
        Mockito.when(mockContract.getSalary()).thenReturn(new BigDecimal("6500.00"));

        Set<Contract> mockContractsSet = new HashSet<>();
        mockContractsSet.add(mockContract);
        Mockito.when(mockCompany.getContracts()).thenReturn(mockContractsSet);

        // when
        List<String[]> dataMatrix = reportingService.compileEmployeeTableData(mockCompany);

        // then
        assertEquals(1, dataMatrix.size());
        String[] row = dataMatrix.get(0);
        assertEquals("101", row[0]);
        assertEquals(empId.toString(), row[1]);
        assertEquals("John Brooks", row[2]);
        assertEquals("MALE", row[3]);
        assertEquals("1992-04-15", row[4]);
        assertEquals("SENIOR_DEVELOPER", row[5]);
        assertEquals("6500.00", row[6]);

        Mockito.verify(mockCompany).getContracts();
    }

    // =========================================================================
    // METHOD UNDER TEST: compileTeamTableData
    // =========================================================================

    @Test
    void testCompileTeamTableData_shouldReturnGroupedRowBlocks_whenTeamsExist() {
        // given
        UUID teamId = UUID.randomUUID();

        // Leader setup via Contract mock
        Employee mockLeader = Mockito.mock(Employee.class);
        Mockito.when(mockLeader.getName()).thenReturn("Alice Manager");
        Contract mockLeaderContract = Mockito.mock(Contract.class);
        Mockito.when(mockLeaderContract.getEmployee()).thenReturn(mockLeader);
        Mockito.when(mockLeaderContract.getPosition()).thenReturn(Position.MANAGER);

        // Contributor setup via Contract mock
        Employee mockContributor = Mockito.mock(Employee.class);
        Mockito.when(mockContributor.getName()).thenReturn("Bob Developer");
        Contract mockMemberContract = Mockito.mock(Contract.class);
        Mockito.when(mockMemberContract.getEmployee()).thenReturn(mockContributor);
        Mockito.when(mockMemberContract.getPosition()).thenReturn(Position.JUNIOR_DEVELOPER);

        Team mockTeam = Mockito.mock(Team.class);
        Mockito.when(mockTeam.getId()).thenReturn(teamId);
        Mockito.when(mockTeam.getManagerContract()).thenReturn(mockLeaderContract);

        Set<Contract> memberContractsSet = new HashSet<>();
        memberContractsSet.add(mockMemberContract);
        Mockito.when(mockTeam.getMemberContracts()).thenReturn(memberContractsSet);

        Set<Team> mockTeamsSet = new HashSet<>();
        mockTeamsSet.add(mockTeam);
        Mockito.when(mockCompany.getTeams()).thenReturn(mockTeamsSet);

        // when
        List<String[]> teamMatrix = reportingService.compileTeamTableData(mockCompany);

        // then
        // Expects exactly 3 rows: Row 0 (Leader details), Row 1 (Member details), Row 2 (Spacer gap layout)
        assertEquals(3, teamMatrix.size());

        String[] leaderRow = teamMatrix.get(0);
        assertEquals(teamId.toString(), leaderRow[0]);
        assertEquals("LEADER: Alice Manager", leaderRow[1]);
        assertEquals("MANAGER", leaderRow[2]);

        String[] memberRow = teamMatrix.get(1);
        assertEquals("", memberRow[0]);
        assertEquals("  - Bob Developer", memberRow[1]);
        assertEquals("JUNIOR_DEVELOPER", memberRow[2]);

        Mockito.verify(mockCompany).getTeams();
    }

    // =========================================================================
    // METHOD UNDER TEST: compileBaseSalariesTableData
    // =========================================================================

    @Test
    void testCompileBaseSalariesTableData_shouldReturnMapPairs_whenFloorsAreConfigured() {
        // given
        Map<Position, BigDecimal> mockSalariesMap = new LinkedHashMap<>();
        mockSalariesMap.put(Position.QA_ENGINEER, new BigDecimal("3200.50"));
        Mockito.when(mockCompany.getPositionMinimumSalaries()).thenReturn(mockSalariesMap);

        // when
        List<String[]> salaryMatrix = reportingService.compileBaseSalariesTableData(mockCompany);

        // then
        assertEquals(1, salaryMatrix.size());
        String[] row = salaryMatrix.get(0);
        assertEquals("QA_ENGINEER", row[0]);
        assertEquals("3200.50", row[1]);

        Mockito.verify(mockCompany).getPositionMinimumSalaries();
    }
}
