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
 * Executes isolated London-school unit tests targeting the ReportingService matrix compilers.
 * Verifies relational stream traversals, sequence sorting, and matrix layout configurations in RAM.
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
    void testCompileEmployeeTableData_shouldReturnSortedStringMatrix_whenActiveContractsExist() {
        // given
        UUID targetId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        Employee mockEmp = Mockito.mock(Employee.class);
        Mockito.when(mockEmp.getId()).thenReturn(targetId);
        Mockito.when(mockEmp.getName()).thenReturn("John Doe");
        Mockito.when(mockEmp.getGender()).thenReturn(Gender.MALE);
        Mockito.when(mockEmp.getBirthDate()).thenReturn(LocalDate.of(1990, 5, 10));
        Mockito.when(mockEmp.getPosition()).thenReturn(Position.SENIOR_DEVELOPER);
        Mockito.when(mockEmp.getSalary()).thenReturn(new BigDecimal("6500.50"));

        Contract mockContract = Mockito.mock(Contract.class);
        Mockito.when(mockContract.getContractNumber()).thenReturn(105);
        Mockito.when(mockContract.getEmployee()).thenReturn(mockEmp);

        Set<Contract> contracts = new HashSet<>();
        contracts.add(mockContract);
        Mockito.when(mockCompany.getContracts()).thenReturn(contracts);

        // when
        List<String[]> matrix = reportingService.compileEmployeeTableData(mockCompany);

        // then
        assertNotNull(matrix, "The matrix compile operation output must never be null");
        assertEquals(1, matrix.size(), "The matrix row count must match the total contract records size");

        String[] row = matrix.get(0);
        assertEquals("105", row[0], "Column 0 must represent the stringified contract index reference");
        assertEquals("00000000-0000-0000-0000-000000000001", row[1]);
        assertEquals("John Doe", row[2]);
        assertEquals("MALE", row[3]);
        assertEquals("1990-05-10", row[4]);
        assertEquals("SENIOR_DEVELOPER", row[5]);
        assertEquals("6500.50", row[6], "Column 6 must map the salary value formatted to exactly two decimal slots");
    }

    // =========================================================================
    // METHOD UNDER TEST: compileTeamTableData
    // =========================================================================

    @Test
    void testCompileTeamTableData_shouldReturnStructuredNestedMatrixBlocks_whenTeamsExist() {
        // given
        Employee mockManager = Mockito.mock(Employee.class);
        Mockito.when(mockManager.getName()).thenReturn("Robert Miller");
        Mockito.when(mockManager.getPosition()).thenReturn(Position.MANAGER);

        Team mockTeam = Mockito.mock(Team.class);
        Mockito.when(mockTeam.getId()).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000010"));
        Mockito.when(mockTeam.getManager()).thenReturn(mockManager);

        // Simulating an empty team contributor list view to evaluate spacing buffers
        Mockito.when(mockTeam.getMembers()).thenReturn(new HashSet<>());

        Set<Team> teams = new HashSet<>();
        teams.add(mockTeam);
        Mockito.when(mockCompany.getTeams()).thenReturn(teams);

        // when
        List<String[]> matrix = reportingService.compileTeamTableData(mockCompany);

        // then: Each team block records exactly 3 display rows (Leader, Spacer placeholder, Empty buffer)
        assertEquals(3, matrix.size());

        assertEquals("00000000-0000-0000-0000-000000000010", matrix.get(0)[0]);
        assertEquals("LEADER: Robert Miller", matrix.get(0)[1]);

        assertEquals("", matrix.get(1)[0]);
        assertEquals("  (No regular member contributors assigned yet)", matrix.get(1)[1]);
    }

    // =========================================================================
    // METHOD UNDER TEST: compileBaseSalariesTableData
    // =========================================================================

    @Test
    void testCompileBaseSalariesTableData_shouldReturnFlatMatrixRows_whenConfigurationsExist() {
        // given
        Map<Position, BigDecimal> positionMinimumSalaries = new LinkedHashMap<>();
        positionMinimumSalaries.put(Position.JUNIOR_DEVELOPER, new BigDecimal("2000.00"));
        Mockito.when(mockCompany.getPositionMinimumSalaries()).thenReturn(positionMinimumSalaries);

        // when
        List<String[]> matrix = reportingService.compileBaseSalariesTableData(mockCompany);

        // then
        assertEquals(1, matrix.size());
        assertEquals("JUNIOR_DEVELOPER", matrix.get(0)[0]);
        assertEquals("2000.00", matrix.get(0)[1]);
    }
}
