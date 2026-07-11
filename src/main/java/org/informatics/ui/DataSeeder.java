package org.informatics.ui;

import org.informatics.data.Company;
import org.informatics.data.Contract;
import org.informatics.data.Employee;
import org.informatics.data.Team;
import org.informatics.data.enums.Gender;
import org.informatics.data.enums.Position;
import org.informatics.service.util.BinarySerializationService;
import org.informatics.service.util.impl.BinarySerializationServiceImplementation;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Production-grade deterministic data seeder. Maps structural company configurations,
 * enforces realistic salary floors, and populates multi-team environments with absolute consistency.
 */
public class DataSeeder {

    public static void main(String[] args) {
        System.out.println("[*] Initializing strict deterministic enterprise data seeding track...");
        BinarySerializationService serializationService = new BinarySerializationServiceImplementation();

        try {
            // =========================================================================
            // COMPANY 1: Vanguard Enterprise Solutions (Standard Baseline Market Rate)
            // =========================================================================
            System.out.println("[*] Provisioning 'Vanguard Enterprise Solutions'...");
            Company vanguard = new Company("Vanguard Enterprise Solutions");
            applyRealisticSalaryFloors(vanguard, new BigDecimal("1.00")); // Baseline scale

            // Establish 2 Distinct Managers and team contributor personnel
            Employee mgrA = new Employee("David Miller", "david.miller@vanguard.com", Gender.MALE, LocalDate.of(1980, 3, 15));
            Employee mgrB = new Employee("Sarah Jenkins", "sarah.jenkins@vanguard.com", Gender.FEMALE, LocalDate.of(1983, 7, 22));
            Employee devSr = new Employee("James Harrison", "james.harrison@vanguard.com", Gender.MALE, LocalDate.of(1988, 11, 5));
            Employee devJr = new Employee("Emily Taylor", "emily.taylor@vanguard.com", Gender.FEMALE, LocalDate.of(1995, 1, 19));
            Employee qaEng = new Employee("Robert Chen", "robert.chen@vanguard.com", Gender.MALE, LocalDate.of(1992, 5, 14));
            Employee designer = new Employee("Jessica Jones", "jessica.jones@vanguard.com", Gender.FEMALE, LocalDate.of(1994, 9, 8));
            Employee helpDesk = new Employee("Michael Chang", "michael.chang@vanguard.com", Gender.MALE, LocalDate.of(1998, 12, 1));
            Employee sysAdmin = new Employee("Amanda Ross", "amanda.ross@vanguard.com", Gender.FEMALE, LocalDate.of(1990, 4, 30));
            Employee dbSpec = new Employee("William Vance", "william.vance@vanguard.com", Gender.MALE, LocalDate.of(1987, 8, 25));
            Employee webDev = new Employee("Elizabeth Fox", "elizabeth.fox@vanguard.com", Gender.FEMALE, LocalDate.of(1996, 2, 10));

            // Wrap explicitly into legally valid contracts (Salaries safely above Vanguard's floors)
            Contract vC1 = new Contract(vanguard.incrementAndGetContractCounter(), mgrA, Position.MANAGER, new BigDecimal("7500.00"));
            Contract vC2 = new Contract(vanguard.incrementAndGetContractCounter(), mgrB, Position.MANAGER, new BigDecimal("7800.00"));
            Contract vC3 = new Contract(vanguard.incrementAndGetContractCounter(), devSr, Position.SENIOR_DEVELOPER, new BigDecimal("6200.00"));
            Contract vC4 = new Contract(vanguard.incrementAndGetContractCounter(), devJr, Position.JUNIOR_DEVELOPER, new BigDecimal("3400.00"));
            Contract vC5 = new Contract(vanguard.incrementAndGetContractCounter(), qaEng, Position.QA_ENGINEER, new BigDecimal("4100.00"));
            Contract vC6 = new Contract(vanguard.incrementAndGetContractCounter(), designer, Position.UI_UX_DESIGNER, new BigDecimal("4300.00"));
            Contract vC7 = new Contract(vanguard.incrementAndGetContractCounter(), helpDesk, Position.HELP_DESK, new BigDecimal("3100.00"));
            Contract vC8 = new Contract(vanguard.incrementAndGetContractCounter(), sysAdmin, Position.SYSTEM_ADMINISTRATOR, new BigDecimal("5200.00"));
            Contract vC9 = new Contract(vanguard.incrementAndGetContractCounter(), dbSpec, Position.DATABASE_SPECIALIST, new BigDecimal("5400.00"));
            Contract vC10 = new Contract(vanguard.incrementAndGetContractCounter(), webDev, Position.WEB_DEVELOPER, new BigDecimal("3800.00"));

            vanguard.addContract(vC1); vanguard.addContract(vC2); vanguard.addContract(vC3);
            vanguard.addContract(vC4); vanguard.addContract(vC5); vanguard.addContract(vC6);
            vanguard.addContract(vC7); vanguard.addContract(vC8); vanguard.addContract(vC9);
            vanguard.addContract(vC10);

            // Structure Multi-Team Frameworks cleanly with zero logic checks
            Team vanguardTeam1 = new Team(vC1); // Led by David Miller
            vanguardTeam1.addMemberContract(vC3);
            vanguardTeam1.addMemberContract(vC4);
            vanguardTeam1.addMemberContract(vC5);
            vanguardTeam1.addMemberContract(vC7);
            vanguard.addTeam(vanguardTeam1);

            Team vanguardTeam2 = new Team(vC2); // Led by Sarah Jenkins
            vanguardTeam2.addMemberContract(vC6);
            vanguardTeam2.addMemberContract(vC8);
            vanguardTeam2.addMemberContract(vC9);
            vanguardTeam2.addMemberContract(vC10);
            vanguard.addTeam(vanguardTeam2);

            serializationService.serialize("company_db_vanguard_enterprise_solutions.ser", vanguard);
            System.out.println("[+] Generated: 'company_db_vanguard_enterprise_solutions.ser'");
            // =========================================================================
            // COMPANY 2: Meridian Global Technologies (Premium High-Tier Market Rate)
            // =========================================================================
            System.out.println("[*] Provisioning 'Meridian Global Technologies'...");
            Company meridian = new Company("Meridian Global Technologies");
            applyRealisticSalaryFloors(meridian, new BigDecimal("1.15")); // 15% higher salary floor baseline scale!

            Employee mrgC = new Employee("Thomas Wright", "thomas.wright@meridian.com", Gender.MALE, LocalDate.of(1978, 1, 12));
            Employee mrgD = new Employee("Ashley Cooper", "ashley.cooper@meridian.com", Gender.FEMALE, LocalDate.of(1982, 6, 11));
            Employee mDevSr = new Employee("Christopher Lee", "christopher.lee@meridian.com", Gender.MALE, LocalDate.of(1985, 10, 4));
            Employee mDevJr = new Employee("Megan Brooks", "megan.brooks@meridian.com", Gender.FEMALE, LocalDate.of(1994, 2, 28));
            Employee mQaEng = new Employee("Brian Kelly", "brian.kelly@meridian.com", Gender.MALE, LocalDate.of(1991, 4, 17));
            Employee mDesigner = new Employee("Rebecca Foster", "rebecca.foster@meridian.com", Gender.FEMALE, LocalDate.of(1993, 8, 22));
            Employee mHelpDesk = new Employee("Daniel Reynolds", "daniel.reynolds@meridian.com", Gender.MALE, LocalDate.of(1997, 11, 3));
            Employee mSysAdmin = new Employee("Patricia Martinez", "patricia.martinez@meridian.com", Gender.FEMALE, LocalDate.of(1989, 3, 14));
            Employee mDbSpec = new Employee("Kevin Bradley", "kevin.bradley@meridian.com", Gender.MALE, LocalDate.of(1986, 7, 19));
            Employee mWebDev = new Employee("Laura Cunningham", "laura.cunningham@meridian.com", Gender.FEMALE, LocalDate.of(1995, 5, 26));

            // Wrap explicitly into legally valid contracts (Salaries safely above Meridian's premium floors)
            Contract mC1 = new Contract(meridian.incrementAndGetContractCounter(), mrgC, Position.MANAGER, new BigDecimal("8800.00"));
            Contract mC2 = new Contract(meridian.incrementAndGetContractCounter(), mrgD, Position.MANAGER, new BigDecimal("9100.00"));
            Contract mC3 = new Contract(meridian.incrementAndGetContractCounter(), mDevSr, Position.SENIOR_DEVELOPER, new BigDecimal("7200.00"));
            Contract mC4 = new Contract(meridian.incrementAndGetContractCounter(), mDevJr, Position.JUNIOR_DEVELOPER, new BigDecimal("3900.00"));
            Contract mC5 = new Contract(meridian.incrementAndGetContractCounter(), mQaEng, Position.QA_ENGINEER, new BigDecimal("4800.00"));
            Contract mC6 = new Contract(meridian.incrementAndGetContractCounter(), mDesigner, Position.UI_UX_DESIGNER, new BigDecimal("5100.00"));
            Contract mC7 = new Contract(meridian.incrementAndGetContractCounter(), mHelpDesk, Position.HELP_DESK, new BigDecimal("3600.00"));
            Contract mC8 = new Contract(meridian.incrementAndGetContractCounter(), mSysAdmin, Position.SYSTEM_ADMINISTRATOR, new BigDecimal("6100.00"));
            Contract mC9 = new Contract(meridian.incrementAndGetContractCounter(), mDbSpec, Position.DATABASE_SPECIALIST, new BigDecimal("6300.00"));
            Contract mC10 = new Contract(meridian.incrementAndGetContractCounter(), mWebDev, Position.WEB_DEVELOPER, new BigDecimal("4400.00"));

            meridian.addContract(mC1); meridian.addContract(mC2); meridian.addContract(mC3);
            meridian.addContract(mC4); meridian.addContract(mC5); meridian.addContract(mC6);
            meridian.addContract(mC7); meridian.addContract(mC8); meridian.addContract(mC9);
            meridian.addContract(mC10);

            Team meridianTeam1 = new Team(mC1); // Led by Thomas Wright
            meridianTeam1.addMemberContract(mC3);
            meridianTeam1.addMemberContract(mC4);
            meridianTeam1.addMemberContract(mC5);
            meridianTeam1.addMemberContract(mC7);
            meridian.addTeam(meridianTeam1);

            Team meridianTeam2 = new Team(mC2); // Led by Ashley Cooper
            meridianTeam2.addMemberContract(mC6);
            meridianTeam2.addMemberContract(mC8);
            meridianTeam2.addMemberContract(mC9);
            meridianTeam2.addMemberContract(mC10);
            meridian.addTeam(meridianTeam2);

            serializationService.serialize("company_db_meridian_global_technologies.ser", meridian);
            System.out.println("[+] Generated: 'company_db_meridian_global_technologies.ser'");

            System.out.println("\n[+] Large-scale deterministic environment seeding complete.");

        } catch (Exception e) {
            System.err.println("[!] Seeder execution failure:");
            e.printStackTrace();
        }
    }

    /**
     * Multiplier-Driven Corporate Salary Configuration.
     * Enforces market-realistic baseline salary floors scaled by a company-specific factor.
     * Scale: HELP_DESK < JUNIOR < WEB < QA <= DESIGNER < SYS_ADMIN < DB_SPEC < SENIOR_DEV < MANAGER
     */
    private static void applyRealisticSalaryFloors(Company company, BigDecimal baseMultiplier) {
        company.setSalaryForPosition(Position.HELP_DESK, new BigDecimal("3000.00").multiply(baseMultiplier));
        company.setSalaryForPosition(Position.JUNIOR_DEVELOPER, new BigDecimal("3200.00").multiply(baseMultiplier));
        company.setSalaryForPosition(Position.WEB_DEVELOPER, new BigDecimal("3500.00").multiply(baseMultiplier));
        company.setSalaryForPosition(Position.QA_ENGINEER, new BigDecimal("4000.00").multiply(baseMultiplier));
        company.setSalaryForPosition(Position.UI_UX_DESIGNER, new BigDecimal("4200.00").multiply(baseMultiplier));
        company.setSalaryForPosition(Position.SYSTEM_ADMINISTRATOR, new BigDecimal("5000.00").multiply(baseMultiplier));
        company.setSalaryForPosition(Position.DATABASE_SPECIALIST, new BigDecimal("5200.00").multiply(baseMultiplier));
        company.setSalaryForPosition(Position.SENIOR_DEVELOPER, new BigDecimal("6000.00").multiply(baseMultiplier));
        company.setSalaryForPosition(Position.MANAGER, new BigDecimal("7000.00").multiply(baseMultiplier));
    }
}
