package org.informatics.service.util.impl;

import org.informatics.data.Contract;
import org.informatics.data.Employee;
import org.informatics.data.enums.Gender;
import org.informatics.data.enums.Position;
import org.informatics.exceptions.DataCorruptionException;
import org.informatics.exceptions.SecurityViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Executes isolated London-school unit tests targeting the raw line parsing, formatting,
 * and verification helper methods of the CsvPayrollService implementation.
 */
class CsvPayrollServiceUnitTest {

    private CsvPayrollServiceImplementation csvServiceImpl;

    @BeforeEach
    void setUp() {
        csvServiceImpl = new CsvPayrollServiceImplementation();
    }

    // =========================================================================
    // METHOD UNDER TEST: getString
    // =========================================================================

    @Test
    void testGetString_shouldFormatCsvRowAndSanitizeCommas_whenContractIsValid() {
        // given
        UUID targetId = UUID.randomUUID();

        Employee mockEmployee = Mockito.mock(Employee.class);
        Mockito.when(mockEmployee.getName()).thenReturn("Smith, John");
        Mockito.when(mockEmployee.getId()).thenReturn(targetId);
        Mockito.when(mockEmployee.getGender()).thenReturn(Gender.MALE);
        Mockito.when(mockEmployee.getBirthDate()).thenReturn(LocalDate.of(1995, 8, 12));
        Mockito.when(mockEmployee.getPosition()).thenReturn(Position.JUNIOR_DEVELOPER);
        Mockito.when(mockEmployee.getSalary()).thenReturn(new BigDecimal("2500.00"));

        Contract mockContract = Mockito.mock(Contract.class);
        Mockito.when(mockContract.getContractNumber()).thenReturn(42);
        Mockito.when(mockContract.getEmployee()).thenReturn(mockEmployee);

        String expectedRow = String.format("42,%s,\"Smith, John\",MALE,1995-08-12,JUNIOR_DEVELOPER,2500.00", targetId);

        // when
        String actualRow = csvServiceImpl.getString(mockContract);

        // then
        assertEquals(expectedRow, actualRow, "getString must sanitize names with quotes and format columns cleanly with commas");

        Mockito.verify(mockContract).getContractNumber();
        Mockito.verify(mockEmployee).getName();
    }

    // =========================================================================
    // METHOD UNDER TEST: buildEmployeeFromTokens
    // =========================================================================

    @Test
    void testBuildEmployeeFromTokens_shouldInstantiateCorrectEmployee_whenTokensAreValid() {
        // given
        String[] mockTokens = {
                "105",
                "da3e218b-82f1-419b-bc39-a9a797f1f1d1",
                "\"Miller, David\"",
                "MALE",
                "1989-11-23",
                "SENIOR_DEVELOPER",
                "6200.00"
        };

        // when
        Employee actualEmployee = csvServiceImpl.buildEmployeeFromTokens(mockTokens);

        // then
        assertNotNull(actualEmployee, "Helper must successfully return a concrete Employee instance reference");
        assertEquals("Miller, David", actualEmployee.getName(), "Should clean out the CSV wrapping escape double quotes");
        assertEquals(Gender.MALE, actualEmployee.getGender());
        assertEquals(LocalDate.of(1989, 11, 23), actualEmployee.getBirthDate());
        assertEquals(Position.SENIOR_DEVELOPER, actualEmployee.getPosition());
        assertEquals(0, new BigDecimal("6200.00").compareTo(actualEmployee.getSalary()), "Salary decimals must map with total accuracy");
    }

    // =========================================================================
    // METHOD UNDER TEST: overrideEmployeeId
    // =========================================================================

    @Test
    void testOverrideEmployeeId_shouldInjectHistoricalUUID_usingReflection() {
        // given
        UUID targetHistoricalId = UUID.randomUUID();
        Employee employee = new Employee("Jane Doe", Gender.FEMALE, LocalDate.of(1994, 2, 14), Position.QA_ENGINEER, new BigDecimal("3200.00"));

        // Assure that our constructor initially assigned a completely different random UUID
        assertNotEquals(targetHistoricalId, employee.getId());

        // when
        csvServiceImpl.overrideEmployeeId(employee, targetHistoricalId);

        // then
        assertEquals(targetHistoricalId, employee.getId(), "Reflection helper must successfully overwrite the final ID with our target UUID");
    }

    // =========================================================================
    // METHOD UNDER TEST: verifyCompanyOwnershipMetadata
    // =========================================================================

    @Test
    void testVerifyCompanyOwnershipMetadata_shouldPass_whenCompanyNamesMatchExactly() {
        // given
        String currentCompany = "Apex Cybernetics";
        String validMetadataRow = "# COMPANY_NAME: Apex Cybernetics";

        // when/then
        assertDoesNotThrow(() -> csvServiceImpl.verifyCompanyOwnershipMetadata(currentCompany, validMetadataRow));
    }

    @Test
    void testVerifyCompanyOwnershipMetadata_shouldThrowSecurityViolationException_whenCompanyNamesMismatch() {
        // given
        String currentCompany = "Informatics LLC";
        String mismatchedMetadataRow = "# COMPANY_NAME: Apex Cybernetics";

        // when/then
        assertThrows(SecurityViolationException.class, () ->
                        csvServiceImpl.verifyCompanyOwnershipMetadata(currentCompany, mismatchedMetadataRow),
                "Should fail fast if the payroll data metadata header points to a different corporate owner");
    }

    // =========================================================================
    // METHOD UNDER TEST: verifyCsvHeaderSchema
    // =========================================================================

    @Test
    void testVerifyCsvHeaderSchema_shouldPass_whenHeaderSchemaIsPerfect() {
        // given
        String validHeader = "ContractNumber,EmployeeID,FullName,Gender,BirthDate,Position,Salary";

        // when/then
        assertDoesNotThrow(() -> csvServiceImpl.verifyCsvHeaderSchema(validHeader));
    }

    @Test
    void testVerifyCsvHeaderSchema_shouldThrowCompanyValidationException_whenHeaderIsCorrupt() {
        // given
        String corruptHeader = "ContractNumber,BadColumnName,FullName,Gender";

        // when/then
        assertThrows(DataCorruptionException.class, () ->
                        csvServiceImpl.verifyCsvHeaderSchema(corruptHeader),
                "Should fail fast if forced to process text fields with corrupt layouts or headers");
    }
}
