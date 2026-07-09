package org.informatics.data;

import org.informatics.data.enums.Position;
import org.informatics.exceptions.InvalidSalaryException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;


class ContractDataModelUnitTest {

    // =========================================================================
    // METHOD UNDER TEST: Contract (Constructor)
    // =========================================================================
    @Test
    void testConstructor_shouldInstantiateCorrectly_whenArgumentsAreValid() {
        // given
        Employee mockEmployee = Mockito.mock(Employee.class);
        BigDecimal validSalary = new BigDecimal("3500.00");

        // when
        Contract contract = new Contract(42, mockEmployee, Position.JUNIOR_DEVELOPER, validSalary);

        // then
        assertEquals(42, contract.getContractNumber());
        assertEquals(mockEmployee, contract.getEmployee());
        assertEquals(Position.JUNIOR_DEVELOPER, contract.getPosition());
        assertEquals(0, validSalary.compareTo(contract.getSalary()));
    }

    @Test
    void testConstructor_shouldThrowInvalidSalaryException_whenInitialSalaryIsNegative() {
        // given
        Employee mockEmployee = Mockito.mock(Employee.class);
        BigDecimal negativeSalary = new BigDecimal("-100.00");

        // when/then
        assertThrows(InvalidSalaryException.class, () ->
                new Contract(1, mockEmployee, Position.QA_ENGINEER, negativeSalary)
        );
    }
}
