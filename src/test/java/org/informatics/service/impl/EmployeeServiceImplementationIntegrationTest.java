package org.informatics.service.impl;

import org.informatics.data.Company;
import org.informatics.data.Contract;
import org.informatics.data.Employee;
import org.informatics.data.Team;
import org.informatics.data.enums.Gender;
import org.informatics.data.enums.Position;
import org.informatics.exceptions.InvalidSalaryException;
import org.informatics.exceptions.SalaryConfigurationException;
import org.informatics.service.EmployeeService;
import org.informatics.service.util.DomainLookupService;
import org.informatics.service.util.impl.DomainLookupServiceImplementation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * <p>Executes domain-level integration tests for the EmployeeService module.</p>
 * <p>Verifies real memory collection modifications, contract indexing, and cascading team deletions.</p>
 */
class EmployeeServiceImplementationIntegrationTest {

    private EmployeeService employeeService;
    private Company company;

    @BeforeEach
    void setUp() {
        DomainLookupService lookupService = new DomainLookupServiceImplementation();
        employeeService = new EmployeeServiceImplementation(lookupService);
        company = new Company("Integration Logistics Ltd");
    }

    // =========================================================================
    // METHOD UNDER TEST: hireEmployee
    // =========================================================================

    @Test
    void testHireEmployee_shouldRegisterContractAndInitializeState_whenArgumentsAreValid() {
        // given
        company.setSalaryForPosition(Position.JUNIOR_DEVELOPER, new BigDecimal("2500.00"));
        String name = "Alexander White";
        String email = "alex.white@logistics.com";
        LocalDate birthDate = LocalDate.now().minusYears(24);

        // when: Service method now yields the active legal Contract entity view
        Contract contract = employeeService.hireEmployee(company, name, email, Gender.MALE, birthDate, Position.JUNIOR_DEVELOPER, new BigDecimal("2700.00"));

        // then
        assertNotNull(contract);
        assertEquals(1, company.getContracts().size());

        Contract createdContract = company.getContracts().iterator().next();
        assertEquals(1, createdContract.getContractNumber());
        assertEquals(contract, createdContract);
        assertEquals("Alexander White", createdContract.getEmployee().getName());
        assertEquals("alex.white@logistics.com", createdContract.getEmployee().getEmail());
        assertEquals(Position.JUNIOR_DEVELOPER, createdContract.getPosition());
    }

    @Test
    void testHireEmployee_shouldThrowSalaryConfigurationException_whenRoleHasNoSalaryFloor() {
        // given
        String name = "Alexander White";
        String email = "alex.white@logistics.com";

        // when/then
        assertThrows(SalaryConfigurationException.class, () ->
                employeeService.hireEmployee(company, name, email, Gender.MALE, LocalDate.now().minusYears(25), Position.SENIOR_DEVELOPER, new BigDecimal("5000.00"))
        );
    }

    @Test
    void testHireEmployee_shouldThrowInvalidSalaryException_whenNegotiatedSalaryIsBelowMinimumFloor() {
        // given
        company.setSalaryForPosition(Position.JUNIOR_DEVELOPER, new BigDecimal("3000.00"));
        String name = "Underpaid Applicant";
        String email = "underpaid@logistics.com";
        LocalDate birthDate = LocalDate.now().minusYears(25);
        BigDecimal substandardSalary = new BigDecimal("2900.00");

        // when/then
        assertThrows(InvalidSalaryException.class, () ->
                        employeeService.hireEmployee(company, name, email, Gender.MALE, birthDate, Position.JUNIOR_DEVELOPER, substandardSalary),
                "Should throw InvalidSalaryException if the negotiated salary falls below the position baseline floor"
        );
    }
    // =========================================================================
    // METHOD UNDER TEST: fireEmployee
    // =========================================================================

    @Test
    void testFireEmployee_shouldPurgeContractAndDissolveTeam_whenTargetWorkerIsAManager() {
        // given
        company.setSalaryForPosition(Position.MANAGER, new BigDecimal("5000.00"));

        // Setup via updated method signature yielding contract objects
        Contract managerContract = employeeService.hireEmployee(company, "Manager", "manager@logistics.com", Gender.MALE, LocalDate.now().minusYears(40), Position.MANAGER, new BigDecimal("5500.00"));

        // Team aggregate is born from the manager contract link
        Team realTeam = new Team(managerContract);
        company.addTeam(realTeam);

        // when
        boolean result = employeeService.fireEmployee(company, managerContract.getEmployee().getId());

        // then
        assertTrue(result);
        assertTrue(company.getContracts().isEmpty());
        assertTrue(company.getTeams().isEmpty(), "The team led by the terminated manager contract must be dissolved immediately");
    }

    @Test
    void testFireEmployee_shouldPurgeContractAndEvictMember_whenTargetWorkerIsAContributor() {
        // given
        company.setSalaryForPosition(Position.MANAGER, new BigDecimal("5000.00"));
        company.setSalaryForPosition(Position.JUNIOR_DEVELOPER, new BigDecimal("2000.00"));

        Contract managerContract = employeeService.hireEmployee(company, "Manager", "manager@logistics.com", Gender.FEMALE, LocalDate.now().minusYears(38), Position.MANAGER, new BigDecimal("5000.00"));
        Contract devContract = employeeService.hireEmployee(company, "Developer", "dev@logistics.com", Gender.MALE, LocalDate.now().minusYears(22), Position.JUNIOR_DEVELOPER, new BigDecimal("2200.00"));

        Team realTeam = new Team(managerContract);
        realTeam.addMemberContract(devContract);
        company.addTeam(realTeam);

        // when
        boolean result = employeeService.fireEmployee(company, devContract.getEmployee().getId());

        // then
        assertTrue(result);
        assertEquals(1, company.getContracts().size(), "Manager's contract line must remain completely active");
        assertTrue(realTeam.getMemberContracts().isEmpty(), "Fired developer contributor contract must be dropped from the member pool");
        assertFalse(company.getTeams().isEmpty(), "Team shell structure itself must remain active");
    }
}
