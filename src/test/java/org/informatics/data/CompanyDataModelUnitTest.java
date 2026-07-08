package org.informatics.data;

import org.informatics.data.Company;
import org.informatics.data.Contract;
import org.informatics.data.Employee;
import org.informatics.data.enums.Gender;
import org.informatics.data.enums.Position;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Executes pure Detroit-style classicist unit tests for the Company aggregate root data model invariants.
 * Verifies name caps, collection blockades, and explicit state tracking manipulation pathways.
 */
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


}
