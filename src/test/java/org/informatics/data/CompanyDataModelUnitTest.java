package org.informatics.data;
import org.informatics.data.enums.Position;
import org.informatics.exceptions.InvalidSalaryException;
import org.junit.jupiter.api.Test;


import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CompanyDataModelUnitTest {

    // =========================================================================
    // METHOD UNDER TEST: Company (Constructor)
    // =========================================================================

    @Test
    void testConstructor_shouldThrowIllegalArgumentException_whenCompanyNameViolatesLimits() {
        // given
        String excessivelyLongTitle = "A".repeat(101);

        // when/then
        assertThrows(IllegalArgumentException.class, () -> new Company("Z"), "Too short");
        assertThrows(IllegalArgumentException.class, () -> new Company(excessivelyLongTitle), "Too long");
    }

    @Test
    void testSetSalaryForPosition_shouldThrowInvalidSalaryException_whenFloorProvidedIsNegative() {
        // given
        Company company = new Company("Invariants Inc");
        BigDecimal negativeSalary = new BigDecimal("-500.00");

        // when/then: The aggregate root must protect its own internal maps
        assertThrows(InvalidSalaryException.class, () ->
                company.setSalaryForPosition(Position.JUNIOR_DEVELOPER, negativeSalary)
        );
    }
}
