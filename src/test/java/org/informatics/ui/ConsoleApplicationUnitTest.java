package org.informatics.ui;

import org.informatics.service.EmployeeService;
import org.informatics.service.TeamService;
import org.informatics.service.FinanceService;
import org.informatics.service.ReportingService;
import org.informatics.service.CompanyPersistenceService;
import org.informatics.service.registry.FileRegistryService;
import org.informatics.service.util.CsvPayrollService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Executes isolated London-school unit tests targeting the keyboard input loop parsing mechanics of ConsoleApplication.
 * Standardizes collaborator mocks as clean class properties to ensure correct chronological initialization order.
 */
class ConsoleApplicationUnitTest {

    private final InputStream originalSystemIn = System.in;

    // Class-level Mocks: Instantiated cleanly once to avoid duplicate method-level boilerplate text
    private final EmployeeService mockEmplService = Mockito.mock(EmployeeService.class);
    private final TeamService mockTeamService = Mockito.mock(TeamService.class);
    private final FinanceService mockFinanceService = Mockito.mock(FinanceService.class);
    private final ReportingService mockReportingService = Mockito.mock(ReportingService.class);
    private final CompanyPersistenceService mockPersistenceService = Mockito.mock(CompanyPersistenceService.class);
    private final FileRegistryService mockFileRegistryService = Mockito.mock(FileRegistryService.class);
    private final CsvPayrollService mockCsvPayrollService = Mockito.mock(CsvPayrollService.class);

    @AfterEach
    void tearDown() {
        // Essential cleanup: always restore the real keyboard system input channel
        System.setIn(originalSystemIn);
    }

    // =========================================================================
    // METHOD UNDER TEST: promptSelectionIndex
    // =========================================================================

    @Test
    void testPromptSelectionIndex_shouldReturnParsedInteger_whenValidIndexIsProvided() {
        // Step 1: Swap System.in FIRST
        //given
        System.setIn(new ByteArrayInputStream("5\n".getBytes()));

        // Step 2: Instantiate ConsoleApplication SECOND so the Scanner captures the fake stream
        ConsoleApplication app = new ConsoleApplication(
                mockEmplService, mockTeamService, mockFinanceService, mockReportingService,
                mockPersistenceService, mockFileRegistryService, mockCsvPayrollService
        );

        // Step 3: Invoke the method
        //when
        int actualSelection = app.promptSelectionIndex("Test Prompt: ", 0, 11);

        // then
        assertEquals(5, actualSelection, "The parsing helper must accurately extract the numeric integer index value.");
    }

    @Test
    void testPromptSelectionIndex_shouldLoopAndRecover_whenFirstEntryIsCorruptTextFormat() {
        // Step 1: Swap System.in FIRST
        //given
        System.setIn(new ByteArrayInputStream("abc\n3\n".getBytes()));

        // Step 2: Instantiate ConsoleApplication SECOND
        ConsoleApplication app = new ConsoleApplication(
                mockEmplService, mockTeamService, mockFinanceService, mockReportingService,
                mockPersistenceService, mockFileRegistryService, mockCsvPayrollService
        );

        // Step 3: Invoke the method
        //when
        int actualSelection = app.promptSelectionIndex("Test Prompt: ", 0, 11);

        // then
        assertEquals(3, actualSelection, "The loop framework must trap the NumberFormatException, ignore the typo text, and parse the next valid number entry.");
    }

    @Test
    void testPromptSelectionIndex_shouldLoopAndRecover_whenFirstEntryViolatesBoundaryLimits() {
        // Step 1: Swap System.in FIRST
        //given
        System.setIn(new ByteArrayInputStream("15\n0\n".getBytes()));

        // Step 2: Instantiate ConsoleApplication SECOND
        ConsoleApplication app = new ConsoleApplication(
                mockEmplService, mockTeamService, mockFinanceService, mockReportingService,
                mockPersistenceService, mockFileRegistryService, mockCsvPayrollService
        );

        // Step 3: Invoke the method
        //when
        int actualSelection = app.promptSelectionIndex("Test Prompt: ", 0, 11);

        // then
        assertEquals(0, actualSelection, "The validation loop must successfully block index values outside the min/max bounds and await a proper choice.");
    }
}
