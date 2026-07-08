package org.informatics.service.util.impl;

import org.informatics.data.Company;
import org.informatics.data.Contract;
import org.informatics.data.Employee;
import org.informatics.data.Team;
import org.informatics.data.enums.Gender;
import org.informatics.data.enums.Position;
import org.informatics.exceptions.FileRegistryException;
import org.informatics.service.util.BinarySerializationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Executes system integration tests targeting the BinarySerializationService implementation.
 * Verifies lower-level Java object streams by writing real rich aggregate states directly
 * onto the physical disk filesystem.
 */
class BinarySerializationServiceImplementationIntegrationTest {

    private BinarySerializationService serializationService;
    private final String testArtifactPath = "integration_test_snapshot.ser";

    @BeforeEach
    void setUp() {
        serializationService = new BinarySerializationServiceImplementation();
    }

    @AfterEach
    void tearDown() {
        File file = new File(testArtifactPath);
        if (file.exists()) {
            file.delete();
        }
    }

    // =========================================================================
    // METHOD UNDER TEST: serialize / deserialize
    // =========================================================================

    @Test
    void shouldWriteAndReadObjectStateCleanly_whenProcessedThroughPhysicalFileStreams() {
        // given
        Company originalCompany = new Company("Informatics Global Corp");
        originalCompany.setSalaryForPosition(Position.MANAGER, new BigDecimal("7500.00"));
        originalCompany.setContractCounter(400);

        Employee manager = new Employee("Stephen Smith", Gender.MALE, LocalDate.of(1980, 8, 24), Position.MANAGER, new BigDecimal("8000.00"));
        Contract originalContract = new Contract(401, manager);
        originalCompany.addContract(originalContract);

        Team team = new Team(manager);
        originalCompany.addTeam(team);

        // when: Execute physical serialization transaction write to local disk
        serializationService.serialize(testArtifactPath, originalCompany);

        // execute physical deserialization recovery read from local disk
        Company restoredCompany = serializationService.deserialize(testArtifactPath, Company.class);

        // then: Verify master scalar primitives are safely recovered
        assertNotNull(restoredCompany, "The restored company instance container must not be null");
        assertEquals("Informatics Global Corp", restoredCompany.getName());
        assertEquals(400, restoredCompany.getContractCounter());

        assertEquals(1, restoredCompany.getContracts().size());
        Contract loadedContract = restoredCompany.getContracts().iterator().next();
        assertEquals(401, loadedContract.getContractNumber());
        assertEquals("Stephen Smith", loadedContract.getEmployee().getName());

        assertEquals(1, restoredCompany.getTeams().size());
        Team loadedTeam = restoredCompany.getTeams().iterator().next();
        assertEquals(loadedContract.getEmployee(), loadedTeam.getManager(),
                "The deserialized team leader instance reference must preserve memory identity mapping with the deserialized contract worker");
    }

    @Test
    void shouldThrowRuntimeException_whenDeserializingFromCompletelyMissingFilePath() {
        // given
        String nonExistentPath = "completely_absent_system_file_marker.ser";

        // when/then: Direct file read miss should capture the underlying IOException wrapper cleanly
        assertThrows(RuntimeException.class, () ->
                        serializationService.deserialize(nonExistentPath, Company.class),
                "Should throw a runtime file exception wrapper if attempting to pull records from a missing disk location"
        );
    }
}
