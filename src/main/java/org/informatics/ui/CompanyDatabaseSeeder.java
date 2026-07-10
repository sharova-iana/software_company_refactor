/*package org.informatics.ui;

import org.informatics.data.Company;
import org.informatics.data.Employee;
import org.informatics.data.Team;
import org.informatics.data.enums.Gender;
import org.informatics.data.enums.Position;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CompanyDatabaseSeeder {

    public static void main(String[] args) {
        CompanyService companyService = new CompanyServiceImplementation();
        //UtilityService utilityService = new UtilityServiceImplementation();

        // 1. Initialize our new second company profile target
        Company apex = new Company("Apex Cybernetics");
        System.out.println("[*] Initializing database seeding for: " + apex.getName());

        // 2. Map distinct base salary floors to EVERY position constant in the enum
        companyService.setSalaryForPosition(apex, Position.MANAGER, new BigDecimal("5000.00"));
        companyService.setSalaryForPosition(apex, Position.SENIOR_DEVELOPER, new BigDecimal("6000.00"));
        companyService.setSalaryForPosition(apex, Position.JUNIOR_DEVELOPER, new BigDecimal("2500.00"));
        companyService.setSalaryForPosition(apex, Position.QA_ENGINEER, new BigDecimal("3000.00"));
        companyService.setSalaryForPosition(apex, Position.UI_UX_DESIGNER, new BigDecimal("3200.00"));
        System.out.println("[+] Base salary thresholds assigned to all roles.");

        // 3. Hire Leadership (Managers)
        Employee chiefManager = companyService.hireEmployee(apex, "Sarah Jenkins", Gender.FEMALE,
                LocalDate.of(1984, 5, 14), Position.MANAGER, new BigDecimal("5500.00"));

        Employee subManager = companyService.hireEmployee(apex, "James Vance", Gender.MALE,
                LocalDate.of(1988, 9, 21), Position.MANAGER, new BigDecimal("5100.00"));

        // 4. Hire Engineering Staff (Asymmetric Salaries)
        Employee dev1 = companyService.hireEmployee(apex, "John Miller", Gender.MALE,
                LocalDate.of(1991, 3, 11), Position.SENIOR_DEVELOPER, new BigDecimal("6800.00"));

        Employee dev2 = companyService.hireEmployee(apex, "Mark Benson", Gender.MALE,
                LocalDate.of(1985, 12, 18), Position.SENIOR_DEVELOPER, new BigDecimal("6100.00"));

        Employee dev3 = companyService.hireEmployee(apex, "Emily Green", Gender.FEMALE,
                LocalDate.of(1996, 6, 25), Position.JUNIOR_DEVELOPER, new BigDecimal("2700.00"));

        Employee qa1 = companyService.hireEmployee(apex, "Alan Brooks", Gender.MALE,
                LocalDate.of(1990, 6, 23), Position.QA_ENGINEER, new BigDecimal("3400.00"));

        Employee qa2 = companyService.hireEmployee(apex, "Grace Harrison", Gender.FEMALE,
                LocalDate.of(1992, 12, 9), Position.QA_ENGINEER, new BigDecimal("3900.00"));

        Employee designer = companyService.hireEmployee(apex, "Amanda Reynolds", Gender.FEMALE,
                LocalDate.of(1994, 12, 10), Position.UI_UX_DESIGNER, new BigDecimal("3500.00"));

        Employee dev4 = companyService.hireEmployee(apex, "David Taylor", Gender.MALE,
                LocalDate.of(1980, 5, 29), Position.SENIOR_DEVELOPER, new BigDecimal("7500.00"));

        System.out.println("[+] Successfully hired " + apex.getContracts().size() + " multi-tiered personnel entries.");

        // 5. Build and configure Team Alpha (Led by Sarah Jenkins)
        Team alphaTeam = companyService.createTeam(apex, chiefManager.getId());
        companyService.addMemberToTeam(apex, alphaTeam.getId(), dev1.getId());
        companyService.addMemberToTeam(apex, alphaTeam.getId(), qa1.getId());
        companyService.addMemberToTeam(apex, alphaTeam.getId(), designer.getId());

        // 6. Build and configure Team Beta (Led by James Vance)
        Team betaTeam = companyService.createTeam(apex, subManager.getId());
        companyService.addMemberToTeam(apex, betaTeam.getId(), dev2.getId());
        companyService.addMemberToTeam(apex, betaTeam.getId(), dev3.getId());
        companyService.addMemberToTeam(apex, betaTeam.getId(), qa2.getId());
        companyService.addMemberToTeam(apex, betaTeam.getId(), dev4.getId());

        System.out.println("[+] Team Alpha and Team Beta structured with mutually exclusive member pools.");

        // 7. Serialize this dataset to disk
        String fileName = "company_db_apex_cybernetics.ser";
        //utilityService.serialize(fileName, apex);

        System.out.println("\n[=] Success! Generated binary workspace store file: '" + fileName + "'");
        System.out.println("[=] You can now boot ConsoleApplication and Load this workspace live.");
    }
}
*/