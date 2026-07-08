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
 * Executes domain-level integration tests for the EmployeeService module.
 * Verifies real memory collection modifications, contract indexing, and cascading team deletions.
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
        LocalDate birthDate = LocalDate.now().minusYears(24);

        // when
        Employee employee = employeeService.hireEmployee(company, name, Gender.MALE, birthDate, Position.JUNIOR_DEVELOPER, new BigDecimal("2700.00"));

        // then
        assertNotNull(employee);
        assertEquals(1, company.getContracts().size());

        Contract createdContract = company.getContracts().iterator().next();
        assertEquals(1, createdContract.getContractNumber());
        assertEquals(employee, createdContract.getEmployee());
        assertEquals("Alexander White", createdContract.getEmployee().getName());
    }

    @Test
    void testHireEmployee_shouldThrowSalaryConfigurationException_whenRoleHasNoSalaryFloor() {
        // given
        // No position baseline floor is registered into the company map context
        String name = "Alexander White";

        // when/then
        assertThrows(SalaryConfigurationException.class, () ->
                employeeService.hireEmployee(company, name, Gender.MALE, LocalDate.now().minusYears(25), Position.SENIOR_DEVELOPER, new BigDecimal("5000.00"))
        );
    }

    @Test
    void testHireEmployee_shouldThrowInvalidSalaryException_whenNegotiatedSalaryIsBelowMinimumFloor() {
        // given: Set up a 3000.00 entry floor for developers
        company.setSalaryForPosition(Position.JUNIOR_DEVELOPER, new BigDecimal("3000.00"));
        String name = "Underpaid Applicant";
        LocalDate birthDate = LocalDate.now().minusYears(25);
        BigDecimal substandardSalary = new BigDecimal("2900.00"); // 100.00 below the floor

        // when/then: The orchestrator must catch the boundary violation and fail fast
        assertThrows(InvalidSalaryException.class, () ->
                        employeeService.hireEmployee(company, name, Gender.MALE, birthDate, Position.JUNIOR_DEVELOPER, substandardSalary),
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
        Employee manager = employeeService.hireEmployee(company, "Manager", Gender.MALE, LocalDate.now().minusYears(40), Position.MANAGER, new BigDecimal("5500.00"));

        // Set up a real team structural link in company memory
        Team realTeam = new Team(manager);
        company.addTeam(realTeam);

        // when
        boolean result = employeeService.fireEmployee(company, manager.getId());

        // then
        assertTrue(result);
        assertTrue(company.getContracts().isEmpty());
        assertTrue(company.getTeams().isEmpty(), "The team led by the terminated manager must be dissolved immediately");
    }

    @Test
    void testFireEmployee_shouldPurgeContractAndEvictMember_whenTargetWorkerIsAContributor() {
        // given
        company.setSalaryForPosition(Position.MANAGER, new BigDecimal("5000.00"));
        company.setSalaryForPosition(Position.JUNIOR_DEVELOPER, new BigDecimal("2000.00"));

        Employee manager = employeeService.hireEmployee(company, "Manager", Gender.FEMALE, LocalDate.now().minusYears(38), Position.MANAGER, new BigDecimal("5000.00"));
        Employee dev = employeeService.hireEmployee(company, "Developer", Gender.MALE, LocalDate.now().minusYears(22), Position.JUNIOR_DEVELOPER, new BigDecimal("2200.00"));

        Team realTeam = new Team(manager);
        realTeam.addMember(dev);
        company.addTeam(realTeam);

        // when
        boolean result = employeeService.fireEmployee(company, dev.getId());

        // then
        assertTrue(result);
        assertEquals(1, company.getContracts().size(), "Manager's contract line must remain active");
        assertTrue(realTeam.getMembers().isEmpty(), "Fired developer contributor must be dropped from the member pool");
        assertFalse(company.getTeams().isEmpty(), "Team shell itself must remain completely active");
    }

}
