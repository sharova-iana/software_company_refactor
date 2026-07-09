package org.informatics.service.util.impl;

import org.informatics.data.Company;
import org.informatics.data.Contract;
import org.informatics.data.Employee;
import org.informatics.data.enums.Gender;
import org.informatics.data.enums.Position;
import org.informatics.exceptions.FileRegistryException;
import org.informatics.exceptions.SecurityViolationException;
import org.informatics.service.util.CsvPayrollService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Executes system integration tests targeting the CsvPayrollService formatting module.
 * Verifies text file disk outputs, schema ownership validation metadata checks, and cross-platform CSV parsing loops.
 */
class CsvPayrollServiceIntegrationTest {

    private Company company;
    private CsvPayrollService csvService;
    private final String testCsvPath = "test_payroll_registry.csv";

    @BeforeEach
    void setUp() {
        company = new Company("CSV Testing Corp");
        csvService = new CsvPayrollServiceImplementation();
    }

    @AfterEach
    void tearDown() {
        File file = new File(testCsvPath);
        if (file.exists()) {
            file.delete();
        }
    }

    // =========================================================================
    // METHOD UNDER TEST: exportPayrollToCsv / importPayrollFromCsv
    // =========================================================================

    @Test
    void shouldExportAndImportPayrollDataPerfectly_whenStateIsProcessedThroughCSVTextStreams() {
        // given
        Employee emp = new Employee("Doe, John \"The Boss\"", "john.boss@csvcorp.com", Gender.MALE, LocalDate.of(1990, 5, 10));

        Contract originalContract = new Contract(105, emp, Position.SENIOR_DEVELOPER, new BigDecimal("6500.50"));

        company.addContract(originalContract);
        company.setContractCounter(105);

        // when
        csvService.exportPayrollToCsv(company, testCsvPath);

        Company targetEmptyCompany = new Company("CSV Testing Corp");
        csvService.importPayrollFromCsv(targetEmptyCompany, testCsvPath);

        // then
        assertEquals(1, targetEmptyCompany.getContracts().size(), "The imported company registry count must equal 1");
        Contract loadedContract = targetEmptyCompany.getContracts().iterator().next();

        assertEquals(originalContract.getContractNumber(), loadedContract.getContractNumber());
        assertEquals(emp.getId(), loadedContract.getEmployee().getId());
        assertEquals(emp.getName(), loadedContract.getEmployee().getName());
        assertEquals("john.boss@csvcorp.com", loadedContract.getEmployee().getEmail());
        assertEquals(Position.SENIOR_DEVELOPER, loadedContract.getPosition());

        // Asserting against the contract compensation boundaries rather than the worker profile
        assertEquals(0, originalContract.getSalary().compareTo(loadedContract.getSalary()));
        assertEquals(105, targetEmptyCompany.getContractCounter());
    }

    @Test
    void shouldThrowSecurityViolationException_whenImportingAFileBelongingToADifferentCompany() {
        // given
        Employee emp = new Employee("Jane Smith", "jane.smith@csvcorp.com", Gender.FEMALE, LocalDate.of(1992, 4, 4));
        Contract contract = new Contract(1, emp, Position.SENIOR_DEVELOPER, new BigDecimal("6000.00"));

        company.addContract(contract);

        // Signed under original company name metadata: "CSV Testing Corp"
        csvService.exportPayrollToCsv(company, testCsvPath);

        // Create an active target workspace with a mismatched company name token
        Company mismatchedCompany = new Company("Mismatched Alternate LLC");

        // when/then
        assertThrows(SecurityViolationException.class, () ->
                        csvService.importPayrollFromCsv(mismatchedCompany, testCsvPath),
                "Should drop execution loop and throw a validation block error if company names collide");
    }

    @Test
    void shouldThrowFileRegistryException_whenImportingMissingFile() {
        // given
        String invalidPath = "completely_missing_payroll_file.csv";

        // when/then
        assertThrows(FileRegistryException.class, () ->
                csvService.importPayrollFromCsv(company, invalidPath));
    }
}
