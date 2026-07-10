package org.informatics.service.impl;

import org.informatics.data.Company;
import org.informatics.data.Contract;
import org.informatics.data.enums.Position;
import org.informatics.exceptions.InvalidSalaryException;
import org.informatics.service.FinanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>Executes pure, strict London-school unit tests targeting the FinanceService implementation module.</p>
 * <p>Following Mockist standards, every collaborator and rich data aggregate dependency is completely mocked,
 * ensuring budget configurations and mathematical averages are evaluated in absolute isolation.</p>
 */
class FinanceServiceImplementationUnitTest {

    private FinanceService financeService;
    private Company mockCompany;

    @BeforeEach
    void setUp() {
        financeService = new FinanceServiceImplementation();
        mockCompany = Mockito.mock(Company.class);
    }

    // =========================================================================
    // METHOD UNDER TEST: setSalaryForPosition
    // =========================================================================

    @Test
    void testSetSalaryForPosition_shouldCallCompanySetSalaryForPosition_whenSalaryIsValid() {
        // given
        Position position = Position.JUNIOR_DEVELOPER;
        BigDecimal validSalary = new BigDecimal("2500.00");

        // when
        financeService.setSalaryForPosition(mockCompany, position, validSalary);

        // then
        Mockito.verify(mockCompany).setSalaryForPosition(position, validSalary);
    }

    // =========================================================================
    // METHOD UNDER TEST: countEmployeesWithSalaryGreaterThan
    // =========================================================================

    @Test
    void testCountEmployeesWithSalaryGreaterThan_shouldReturnCorrectCount_whenContractsExist() {
        // given
        BigDecimal threshold = new BigDecimal("4000.00");

        // Contract A: Earning above threshold (Resides directly on Contract now)
        Contract mockContractA = Mockito.mock(Contract.class);
        Mockito.when(mockContractA.getSalary()).thenReturn(new BigDecimal("5000.00"));

        // Contract B: Earning below threshold (Resides directly on Contract now)
        Contract mockContractB = Mockito.mock(Contract.class);
        Mockito.when(mockContractB.getSalary()).thenReturn(new BigDecimal("3000.00"));

        Set<Contract> mockContractsSet = new HashSet<>();
        mockContractsSet.add(mockContractA);
        mockContractsSet.add(mockContractB);

        Mockito.when(mockCompany.getContracts()).thenReturn(mockContractsSet);

        // when
        long actualCount = financeService.countEmployeesWithSalaryGreaterThan(mockCompany, threshold);

        // then
        assertEquals(1, actualCount, "Should count only the contracts whose salaries strictly exceed the threshold balance");
        Mockito.verify(mockCompany).getContracts();
    }

    // =========================================================================
    // METHOD UNDER TEST: calculateAverageSalaryForPosition
    // =========================================================================

    @Test
    void testCalculateAverageSalaryForPosition_shouldReturnZero_whenNoEmployeesMatchPosition() {
        // given
        Position targetPosition = Position.UI_UX_DESIGNER;

        // Return an empty contracts set to simulate zero position matches
        Mockito.when(mockCompany.getContracts()).thenReturn(new HashSet<>());

        // when
        BigDecimal average = financeService.calculateAverageSalaryForPosition(mockCompany, targetPosition);

        // then
        assertEquals(0, BigDecimal.ZERO.compareTo(average), "Should safely return BigDecimal.ZERO if no workers exist in position tier");
        Mockito.verify(mockCompany).getContracts();
    }

    @Test
    void testCalculateAverageSalaryForPosition_shouldReturnPrecisionMean_whenMatchingEmployeesExist() {
        // given
        Position targetPosition = Position.SENIOR_DEVELOPER;

        // Contract 1: Senior Developer earning 6000 (Position and Salary mapped directly on Contract)
        Contract mockContract1 = Mockito.mock(Contract.class);
        Mockito.when(mockContract1.getPosition()).thenReturn(Position.SENIOR_DEVELOPER);
        Mockito.when(mockContract1.getSalary()).thenReturn(new BigDecimal("6000.00"));

        // Contract 2: Senior Developer earning 7500 (Position and Salary mapped directly on Contract)
        Contract mockContract2 = Mockito.mock(Contract.class);
        Mockito.when(mockContract2.getPosition()).thenReturn(Position.SENIOR_DEVELOPER);
        Mockito.when(mockContract2.getSalary()).thenReturn(new BigDecimal("7500.00"));

        // Contract 3: QA Engineer earning 4000 (Should be safely ignored by position filter stream)
        Contract mockContract3 = Mockito.mock(Contract.class);
        Mockito.when(mockContract3.getPosition()).thenReturn(Position.QA_ENGINEER);
        Mockito.when(mockContract3.getSalary()).thenReturn(new BigDecimal("4000.00"));

        Set<Contract> mockContractsSet = new HashSet<>();
        mockContractsSet.add(mockContract1);
        mockContractsSet.add(mockContract2);
        mockContractsSet.add(mockContract3);

        Mockito.when(mockCompany.getContracts()).thenReturn(mockContractsSet);

        // Expected mean calculation: (6000 + 7500) / 2 = 6750.00
        BigDecimal expectedAverage = new BigDecimal("6750.00");

        // when
        BigDecimal actualAverage = financeService.calculateAverageSalaryForPosition(mockCompany, targetPosition);

        // then
        assertEquals(0, expectedAverage.compareTo(actualAverage), "Should calculate the precise average mean scaled to 2 decimal places half-up");
        Mockito.verify(mockCompany).getContracts();
    }
}
