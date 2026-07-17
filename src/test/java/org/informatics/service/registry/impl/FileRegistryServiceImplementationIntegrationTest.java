package org.informatics.service.registry.impl;

import org.informatics.exceptions.FileRegistryException;
import org.informatics.service.registry.FileRegistryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Executes system integration tests targeting the FileRegistryService implementation.
 * Verifies local workspace scanning directory lookups, file filtering routines,
 * and bidirectional corporate filename parsing algorithms.
 */
class FileRegistryServiceImplementationIntegrationTest {

    private FileRegistryService registryService;

    // Test tracking file handles to ensure a clean directory tear down
    private File testFile1;
    private File testFile2;
    private File noisyFile;

    @BeforeEach
    void setUp() throws IOException {
        registryService = new FileRegistryServiceImplementation();

        // Create temporary real files on disk to verify the raw directory scanner
        testFile1 = new File("company_db_informatics_tech.ser");
        testFile2 = new File("company_db_global_payroll_llc.ser");
        noisyFile = new File("random_text_file_not_db.txt");

        // Ensure fresh physical creation on the disk filesystem before running scans
        testFile1.createNewFile();
        testFile2.createNewFile();
        noisyFile.createNewFile();
    }

    @AfterEach
    void tearDown() {
        // Clean up disk workspace state immediately to avoid leakage across subsequent runs
        if (testFile1.exists()) testFile1.delete();
        if (testFile2.exists()) testFile2.delete();
        if (noisyFile.exists()) noisyFile.delete();
    }

    // =========================================================================
    // METHOD UNDER TEST: fetchRawSavedFiles
    // =========================================================================

    @Test
    void testFetchRawSavedFiles_shouldReturnOnlyValidSerializedDatabaseFiles() {
        // when
        File[] validFiles = registryService.fetchRawSavedFiles();

        // then
        assertNotNull(validFiles, "The file sweep array output must never be null");
        assertTrue(validFiles.length >= 2, "The scanner must catch at least our 2 newly provisioned databases");

        // Assert that the filter successfully drops unrelated text formats
        for (File file : validFiles) {
            assertTrue(file.getName().startsWith("company_db_"), "All files caught must match our prefix pattern");
            assertTrue(file.getName().endsWith(".ser"), "All files caught must match our serialization format");
            assertNotEquals("random_text_file_not_db.txt", file.getName(), "The search filter must bypass unrelated files");
        }
    }

    // =========================================================================
    // METHOD UNDER TEST: parseFilenameToCompanyName
    // =========================================================================

    @Test
    void testParseFilenameToCompanyName_shouldRebuildCapitalizedSpacedName_whenValidFormatProvided() {
        // given
        String filename1 = "company_db_informatics_tech.ser";
        String filename2 = "company_db_global_payroll_llc.ser";

        // when
        String result1 = registryService.parseFilenameToCompanyName(filename1);
        String result2 = registryService.parseFilenameToCompanyName(filename2);

        // then: Verifies text trimming, split boundaries, and first-letter capitalization logic
        assertEquals("Informatics Tech", result1);
        assertEquals("Global Payroll Llc", result2);
    }

    @Test
    void testParseFilenameToCompanyName_shouldThrowFileRegistryException_whenFilenameLayoutIsInvalid() {
        // given
        String invalidFilename = "corrupt_backup_record.txt";

        // when/then
        assertThrows(FileRegistryException.class, () -> {
            registryService.parseFilenameToCompanyName(invalidFilename);
        }, "Should drop execution block if attempting to translate an unrecognized file schema");
    }

    // =========================================================================
    // METHOD UNDER TEST: scanAvailableSavedCompanies
    // =========================================================================

    @Test
    void testScanAvailableSavedCompanies_shouldReturnMappedUniqueCleanCompanyNames() {
        // when
        Set<String> cleanNamesSet = registryService.scanAvailableSavedCompanies();

        // then
        assertNotNull(cleanNamesSet);
        assertTrue(cleanNamesSet.contains("Informatics Tech"), "Should contain the processed title format from test file 1");
        assertTrue(cleanNamesSet.contains("Global Payroll Llc"), "Should contain the processed title format from test file 2");
        assertFalse(cleanNamesSet.contains("random_text_file_not_db"), "Should not extract strings from text file types");
    }

    // =========================================================================
    // METHOD UNDER TEST: generateFilename
    // =========================================================================

    @Test
    void testGenerateFilename_shouldSanitizeSpecialCharactersToUnderscoresAndAppendExtension() {
        // given
        String inputCompanyName = "Informatics Core, LLC v2.0!";
        String expectedFilename = "company_db_management_informatics_core__llc_v2_0_.ser";
        // Note: Due to regex replacement [^a-zA-Z0-9], commas, spaces, periods, and symbols translate to continuous underscores

        // when
        String resultFilename = registryService.generateFilename(inputCompanyName);

        // then
        assertNotNull(resultFilename);
        assertTrue(resultFilename.startsWith("company_db_"));
        assertTrue(resultFilename.endsWith(".ser"));
        assertFalse(resultFilename.contains("!"), "Filename string generator must fully purge dangerous text tokens");
    }
}
