package org.informatics.service.impl;

import org.informatics.data.Company;
import org.informatics.data.enums.Position;
import org.informatics.data.enums.Gender;
import org.informatics.service.FinanceService;
import org.informatics.service.util.DomainLookupService;
import org.informatics.service.util.impl.DomainLookupServiceImplementation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Executes domain-level sociable integration tests for the FinanceService module.
 * Verifies live compensation math and baseline configuration maps across real objects.
 */
class FinanceServiceImplementationIntegrationTest {

    private FinanceService financeService;
    private EmployeeServiceImplementation emplService;
    private Company company;

    @BeforeEach
    void setUp() {
        DomainLookupService lookupService = new DomainLookupServiceImplementation();
        emplService = new EmployeeServiceImplementation(lookupService);
        financeService = new FinanceServiceImplementation();
        company = new Company("Finance Integration LLC");
    }

    // =========================================================================
    // METHOD UNDER TEST: setSalaryForPosition
    // =========================================================================

    @Test
    void testSetSalaryForPosition_shouldStoreFloorInConfigurationMap_whenValueIsValid() {
        // given
        BigDecimal floorSalary = new BigDecimal("5000.00");

        // when
        financeService.setSalaryForPosition(company, Position.SENIOR_DEVELOPER, floorSalary);

        // then
        BigDecimal storedFloor = company.getPositionMinimumSalaries().get(Position.SENIOR_DEVELOPER);
        assertNotNull(storedFloor, "The minimum salary map must hold a configuration for the assigned position");
        assertEquals(0, floorSalary.compareTo(storedFloor),
                "The stored floor value must equal the assigned baseline configuration");

    }

    // =========================================================================
    // METHOD UNDER TEST: countEmployeesWithSalaryGreaterThan
    // =========================================================================

    @Test
    void testCountEmployeesWithSalaryGreaterThan_shouldReturnCorrectCount_whenSalariesCrossThreshold() {
        // given
        financeService.setSalaryForPosition(company, Position.SENIOR_DEVELOPER, new BigDecimal("5000.00"));

        emplService.hireEmployee(company, "Joe Smith", Gender.MALE, LocalDate.now().minusYears(32), Position.SENIOR_DEVELOPER, new BigDecimal("5500.00"));
        emplService.hireEmployee(company, "Sally Green", Gender.FEMALE, LocalDate.now().minusYears(34), Position.SENIOR_DEVELOPER, new BigDecimal("6500.00"));

        BigDecimal evaluationThreshold = new BigDecimal("6000.00");

        // when
        long actualCount = financeService.countEmployeesWithSalaryGreaterThan(company, evaluationThreshold);

        // then
        assertEquals(1, actualCount, "The calculation must only count employees whose salaries strictly exceed the threshold balance");
    }

    // =========================================================================
    // METHOD UNDER TEST: calculateAverageSalaryForPosition
    // =========================================================================

    @Test
    void testCalculateAverageSalaryForPosition_shouldReturnPrecisionMean_whenMatchingContractsAreActive() {
        // given
        financeService.setSalaryForPosition(company, Position.SENIOR_DEVELOPER, new BigDecimal("5000.00"));

        emplService.hireEmployee(company, "Joe Smith", Gender.MALE, LocalDate.now().minusYears(32), Position.SENIOR_DEVELOPER, new BigDecimal("5500.00"));
        emplService.hireEmployee(company, "Sally Green", Gender.FEMALE, LocalDate.now().minusYears(34), Position.SENIOR_DEVELOPER, new BigDecimal("6500.00"));

        BigDecimal expectedMean = new BigDecimal("6000.00");

        // when
        BigDecimal actualAverage = financeService.calculateAverageSalaryForPosition(company, Position.SENIOR_DEVELOPER);

        // then
        assertEquals(0, expectedMean.compareTo(actualAverage), "Mean math verification: (5500 + 6500) / 2 must equal exactly 6000.00 scaled half-up");
    }
}
