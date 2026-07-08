package org.informatics.data;

import org.informatics.data.Contract;
import org.informatics.data.Employee;
import org.informatics.data.enums.Gender;
import org.informatics.data.enums.Position;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Executes pure Detroit-style classicist unit tests for the Contract data model invariants.
 * Verifies key parameters, sequential boundaries, and dependency bounds.
 */
class ContractDataModelUnitTest {

    // =========================================================================
    // METHOD UNDER TEST: Contract (Constructor)
    // =========================================================================

    @Test
    void testConstructor_shouldInstantiateCorrectly_whenSequenceIsPositive() {
        // given
        int validContractNum = 105;
        Employee employee = new Employee("Bob Jones", Gender.MALE, LocalDate.now().minusYears(35), Position.MANAGER, new BigDecimal("5000"));

        // when
        Contract contract = new Contract(validContractNum, employee);

        // then
        assertEquals(105, contract.getContractNumber());
        assertEquals(employee, contract.getEmployee());
    }

    @Test
    void testConstructor_shouldThrowIllegalArgumentException_whenSequenceIsNegativeOrZero() {
        // given
        int corruptZeroIndex = 0;
        int corruptNegativeIndex = -42;
        Employee employee = new Employee("Bob Jones", Gender.MALE, LocalDate.now().minusYears(35), Position.MANAGER, new BigDecimal("5000"));

        // when/then
        assertThrows(IllegalArgumentException.class, () -> new Contract(corruptZeroIndex, employee));
        assertThrows(IllegalArgumentException.class, () -> new Contract(corruptNegativeIndex, employee));
    }
}
