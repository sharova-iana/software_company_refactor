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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Executes domain-level sociable integration tests for the ReportingService module.
 * Verifies live collection stream mappings, sorting order, and table row conversions without any mocking.
 */
class ReportingServiceImplementationIntegrationTest {

    private ReportingService reportingService;
    private Company company;

    @BeforeEach
    void setUp() {
        reportingService = new ReportingServiceImplementation();
        company = new Company("Reporting Integration LLC");
    }

    // =========================================================================
    // METHOD UNDER TEST: compileEmployeeTableData
    // =========================================================================

    @Test
    void testCompileEmployeeTableData_shouldReturnOrderedMatrixRows_whenContractsAreActive() {
        // given
        company.setSalaryForPosition(Position.SENIOR_DEVELOPER, new BigDecimal("5000.00"));
        company.setSalaryForPosition(Position.JUNIOR_DEVELOPER, new BigDecimal("2000.00"));

        Employee emp1 = new Employee("Alice Smith", "alice.smith@informatics.com", Gender.FEMALE, LocalDate.now().minusYears(30));
        Employee emp2 = new Employee("Bob Jones", "bob.jones@informatics.com", Gender.MALE, LocalDate.now().minusYears(24));

        Contract contract1 = new Contract(101, emp1, Position.SENIOR_DEVELOPER, new BigDecimal("5500.00"));
        Contract contract2 = new Contract(102, emp2, Position.JUNIOR_DEVELOPER, new BigDecimal("2500.00"));

        company.addContract(contract2);
        company.addContract(contract1);

        // when
        List<String[]> matrix = reportingService.compileEmployeeTableData(company);

        // then
        assertNotNull(matrix);
        assertEquals(2, matrix.size());

        // Row 1 must be Contract #101 due to sequential sorting invariants
        String[] row1 = matrix.get(0);
        assertEquals("101", row1[0]);
        assertEquals("Alice Smith", row1[2]);
        assertEquals("SENIOR_DEVELOPER", row1[5]);
        assertEquals("5500.00", row1[6]);

        // Row 2 must be Contract #102
        String[] row2 = matrix.get(1);
        assertEquals("102", row2[0]);
        assertEquals("Bob Jones", row2[2]);
        assertEquals("JUNIOR_DEVELOPER", row2[5]);
        assertEquals("2500.00", row2[6]);
    }
    // =========================================================================
    // METHOD UNDER TEST: compileTeamTableData
    // =========================================================================

    @Test
    void testCompileTeamTableData_shouldReturnGroupedRowsWithContributors_whenTeamsAreActive() {
        // given
        company.setSalaryForPosition(Position.MANAGER, new BigDecimal("6000.00"));
        company.setSalaryForPosition(Position.JUNIOR_DEVELOPER, new BigDecimal("2000.00"));

        Employee manager = new Employee("Charles Green", "charles.green@informatics.com", Gender.MALE, LocalDate.now().minusYears(42));
        Employee developer = new Employee("David Vance", "david.vance@informatics.com", Gender.MALE, LocalDate.now().minusYears(26));

        Contract managerContract = new Contract(1, manager, Position.MANAGER, new BigDecimal("6500.00"));
        Contract devContract = new Contract(2, developer, Position.JUNIOR_DEVELOPER, new BigDecimal("2800.00"));

        company.addContract(managerContract);
        company.addContract(devContract);

        // Team aggregate is born from and tracks active Contract wrappers
        Team team = new Team(managerContract);
        team.addMemberContract(devContract);
        company.addTeam(team);

        // when
        List<String[]> matrix = reportingService.compileTeamTableData(company);

        // then: Layout maps 1 Leader row, 1 Member row, and 1 spacing separation buffer row
        assertEquals(3, matrix.size());

        // Row 1: Team identification header block
        String[] leaderRow = matrix.get(0);
        assertEquals(team.getId().toString(), leaderRow[0]);
        assertEquals("LEADER: Charles Green", leaderRow[1]);
        assertEquals("MANAGER", leaderRow[2]);

        // Row 2: Member contributor row block
        String[] memberRow = matrix.get(1);
        assertEquals("", memberRow[0], "Team ID column must remain empty for visual grouping spacing");
        assertEquals("  - David Vance", memberRow[1]);
        assertEquals("JUNIOR_DEVELOPER", memberRow[2]);
    }

    // =========================================================================
    // METHOD UNDER TEST: compileBaseSalariesTableData
    // =========================================================================

    @Test
    void testCompileBaseSalariesTableData_shouldReturnConfiguredEntries_whenMapIsPopulated() {
        // given
        company.setSalaryForPosition(Position.MANAGER, new BigDecimal("6000.00"));

        // when
        List<String[]> matrix = reportingService.compileBaseSalariesTableData(company);

        // then
        assertNotNull(matrix);
        assertEquals(1, matrix.size());
        assertEquals("MANAGER", matrix.get(0)[0]);
        assertEquals("6000.00", matrix.get(0)[1]);
    }
}
