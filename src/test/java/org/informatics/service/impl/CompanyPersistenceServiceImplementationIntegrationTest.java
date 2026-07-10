package org.informatics.service.impl;

import org.informatics.data.Company;
import org.informatics.data.Contract;
import org.informatics.data.Employee;
import org.informatics.data.Team;
import org.informatics.data.enums.Gender;
import org.informatics.data.enums.Position;
import org.informatics.service.CompanyPersistenceService;
import org.informatics.service.util.BinarySerializationService;
import org.informatics.service.util.impl.BinarySerializationServiceImplementation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Executes system integration tests targeting the CompanyPersistenceService module.
 * Verifies deep object binary serializations and complete relational schema restorations on the local filesystem.
 */
class CompanyPersistenceServiceImplementationIntegrationTest {

    private CompanyPersistenceService persistenceService;
    private final String testBinaryPath = "test_company_workspace.ser";

    @BeforeEach
    void setUp() {
        BinarySerializationService binaryService = new BinarySerializationServiceImplementation();
        persistenceService = new CompanyPersistenceServiceImplementation(binaryService);
    }

    @AfterEach
    void tearDown() {
        File file = new File(testBinaryPath);
        if (file.exists()) {
            file.delete();
        }
    }
    // =========================================================================
    // METHOD UNDER TEST: saveWorkspace / loadWorkspace
    // =========================================================================

    @Test
    void shouldSerializeAndDeserializeCompleteCompanyAggregateObject_whenProcessedThroughBinaryStreams() {
        // given
        Company originalCompany = new Company("Informatics Core LLC");
        originalCompany.setSalaryForPosition(Position.MANAGER, new BigDecimal("6000.00"));
        originalCompany.setContractCounter(1);

        Employee manager = new Employee("Jack Doe", "jack.doe@informatics.com", Gender.MALE, LocalDate.of(1985, 4, 12));
        Contract contract = new Contract(1, manager, Position.MANAGER, new BigDecimal("6500.00"));
        originalCompany.addContract(contract);

        // Team aggregate tracks legal contract links directly now
        Team team = new Team(contract);
        originalCompany.addTeam(team);

        // when
        persistenceService.saveWorkspace(originalCompany, testBinaryPath);
        Company restoredCompany = persistenceService.loadWorkspace(testBinaryPath);

        // then
        assertNotNull(restoredCompany, "Deserialized entity state container must not be null");
        assertEquals("Informatics Core LLC", restoredCompany.getName());
        assertEquals(1, restoredCompany.getContractCounter());

        // Deep verification: Verify active contracts are completely recovered
        assertEquals(1, restoredCompany.getContracts().size());
        Contract loadedContract = restoredCompany.getContracts().iterator().next();
        assertEquals(1, loadedContract.getContractNumber());
        assertEquals("Jack Doe", loadedContract.getEmployee().getName());
        assertEquals("jack.doe@informatics.com", loadedContract.getEmployee().getEmail());
        assertEquals(Position.MANAGER, loadedContract.getPosition());
        assertEquals(0, new BigDecimal("6500.00").compareTo(loadedContract.getSalary()));

        // Deep verification: Verify relational company organizational structures remain active
        assertEquals(1, restoredCompany.getTeams().size());
        Team loadedTeam = restoredCompany.getTeams().iterator().next();

        // Asserting against the contract layer to confirm deep reference identity integrity is preserved over the stream
        assertEquals(loadedContract.getEmployee(), loadedTeam.getManagerContract().getEmployee(),
                "The restored team leader instance must point to the identical deserialized personnel record");
    }
}
