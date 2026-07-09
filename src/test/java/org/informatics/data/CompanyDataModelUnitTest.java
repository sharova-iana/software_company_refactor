package org.informatics.data;
import org.junit.jupiter.api.Test;


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


}
