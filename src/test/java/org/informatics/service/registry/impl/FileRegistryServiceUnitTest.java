package org.informatics.service.registry.impl;

import org.informatics.exceptions.FileRegistryException;
import org.informatics.service.registry.FileRegistryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Executes isolated unit tests targeting the text processing logic of FileRegistryService.
 * Verifies filename generation strings and capitalization parsing boundaries in RAM isolation.
 */
class FileRegistryServiceUnitTest {

    private FileRegistryService registryService;

    @BeforeEach
    void setUp() {
        registryService = new FileRegistryServiceImplementation();
    }

    // =========================================================================
    // METHOD UNDER TEST: parseFilenameToCompanyName
    // =========================================================================

    @Test
    void testParseFilenameToCompanyName_shouldConvertCorrectly_whenFilenameIsValid() {
        // given
        String rawFile = "company_db_apex_cybernetics.ser";
        String expected = "Apex Cybernetics";

        // when
        String actual = registryService.parseFilenameToCompanyName(rawFile);

        // then
        assertEquals(expected, actual, "Filenames must cleanly map to properly spaced capital casing titles");
    }

    @Test
    void testParseFilenameToCompanyName_shouldThrowIllegalArgumentException_whenFormatIsCorrupt() {
        // given
        String corruptFile = "invalid_document_dump.txt";

        // when/then
        assertThrows(FileRegistryException.class, () ->
                        registryService.parseFilenameToCompanyName(corruptFile),
                "Should fail fast if forced to handle files out of the tracking scope signature");
    }

    // =========================================================================
    // METHOD UNDER TEST: generateFilename
    // =========================================================================

    @Test
    void testGenerateFilename_shouldFormatSafely_whenCompanyNameHasSpaces() {
        // given
        String companyName = "Informatics Studio Tech";
        String expected = "company_db_informatics_studio_tech.ser";

        // when
        String actual = registryService.generateFilename(companyName);

        // then
        assertEquals(expected, actual, "Corporate spaces and capitals must fold down into standard safe underscores");
    }
}
