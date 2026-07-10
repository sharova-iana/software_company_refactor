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
import java.util.ArrayList;
import java.util.List;

/**
 * Production infrastructure data seeder responsible for generating a large-scale,
 * realistic corporate sandbox database tracking all 9 positions and multiple teams.
 */
public class DataSeeder {

    public static void main(String[] args) {
        System.out.println("[*] Initializing large-scale corporate data seeding track...");
        BinarySerializationService serializationService = new BinarySerializationServiceImplementation();

        try {
            // =========================================================================
            // COMPANY 1: Vanguard Enterprise Solutions
            // =========================================================================
            System.out.println("[*] Generating database for 'Vanguard Enterprise Solutions'...");
            Company vanguard = new Company("Vanguard Enterprise Solutions");
            configurePositionFloors(vanguard, new BigDecimal("2500.00"), new BigDecimal("3500.00"), new BigDecimal("5500.00"));

            String[] names1 = {
                    "David Miller", "Sarah Jenkins", "James Harrison", "Emily Taylor", "Robert Chen",
                    "Jessica Jones", "Michael Chang", "Amanda Ross", "William Vance", "Elizabeth Fox",
                    "Thomas Wright", "Ashley Cooper", "Christopher Lee", "Megan Brooks", "Brian Kelly"
            };
            String[] domains1 = { "vanguard.com", "vanguard-solutions.net" };

            List<Contract> vanguardContracts = seedWorkforceRoster(vanguard, names1, domains1);

            // Establish separate corporate teams under different manager contracts
            List<Contract> managersVanguard = filterContractsByPosition(vanguardContracts, Position.MANAGER);
            List<Contract> staffVanguard = filterContractsByOppositePosition(vanguardContracts, Position.MANAGER);

            if (managersVanguard.size() >= 2) {
                // Team Alpha
                Team teamAlpha = new Team(managersVanguard.get(0));
                for (int i = 0; i < staffVanguard.size() / 2; i++) {
                    teamAlpha.addMemberContract(staffVanguard.get(i));
                }
                vanguard.addTeam(teamAlpha);

                // Team Beta
                Team teamBeta = new Team(managersVanguard.get(1));
                for (int i = staffVanguard.size() / 2; i < staffVanguard.size(); i++) {
                    teamBeta.addMemberContract(staffVanguard.get(i));
                }
                vanguard.addTeam(teamBeta);
            }

            serializationService.serialize("company_db_vanguard_enterprise_solutions.ser", vanguard);
            System.out.println("[+] Generated: 'company_db_vanguard_enterprise_solutions.ser'");

            // =========================================================================
            // COMPANY 2: Meridian Global Technologies
            // =========================================================================
            System.out.println("[*] Generating database for 'Meridian Global Technologies'...");
            Company meridian = new Company("Meridian Global Technologies");
            configurePositionFloors(meridian, new BigDecimal("2800.00"), new BigDecimal("4000.00"), new BigDecimal("6000.00"));

            String[] names2 = {
                    "John Saunders", "Rebecca Foster", "Daniel Reynolds", "Patricia Martinez", "Kevin Bradley",
                    "Laura Cunningham", "Matthew Henderson", "Rachel Sullivan", "Andrew Knight", "Nicole Patterson",
                    "Timothy Stevens", "Christine Myers", "George Higgins", "Barbara Graham", "Jeffrey Hayes"
            };
            String[] domains2 = { "meridian-global.com", "meridian-tech.org" };

            List<Contract> meridianContracts = seedWorkforceRoster(meridian, names2, domains2);

            List<Contract> managersMeridian = filterContractsByPosition(meridianContracts, Position.MANAGER);
            List<Contract> staffMeridian = filterContractsByOppositePosition(meridianContracts, Position.MANAGER);

            if (managersMeridian.size() >= 2) {
                // Operations Team
                Team opsTeam = new Team(managersMeridian.get(0));
                for (int i = 0; i < staffMeridian.size() / 2; i++) {
                    opsTeam.addMemberContract(staffMeridian.get(i));
                }
                meridian.addTeam(opsTeam);

                // Innovation Team
                Team innovationTeam = new Team(managersMeridian.get(1));
                for (int i = staffMeridian.size() / 2; i < staffMeridian.size(); i++) {
                    innovationTeam.addMemberContract(staffMeridian.get(i));
                }
                meridian.addTeam(innovationTeam);
            }

            serializationService.serialize("company_db_meridian_global_technologies.ser", meridian);
            System.out.println("[+] Generated: 'company_db_meridian_global_technologies.ser'");

            System.out.println("\n[+] Large-scale data environments successfully populated and active!");

        } catch (Exception e) {
            System.err.println("[!] Critical error executing binary stream data generation loops:");
            e.printStackTrace();
        }
    }

    /**
     * Loops through an array of regular English names and maps them evenly across all 
     * 9 corporate positions, guaranteeing at least one employee sits in each role bucket.
     */
    private static List<Contract> seedWorkforceRoster(Company company, String[] names, String[] domains) {
        List<Contract> createdContracts = new ArrayList<>();
        Position[] allPositions = Position.values();

        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            // Cycle through all positions evenly to eliminate empty role tracking blocks
            Position position = allPositions[i % allPositions.length];

            // Build a clean corporate lowercase email string
            String cleanEmail = name.toLowerCase().replace(" ", ".") + "@" + domains[i % domains.length];
            Gender gender = (i % 2 == 0) ? Gender.MALE : Gender.FEMALE;

            // Stagger ages chronologically between 25 and 55 years old
            LocalDate birthDate = LocalDate.now().minusYears(25 + (i * 2 % 30));

            Employee employee = new Employee(name, cleanEmail, gender, birthDate);

            // Extract the minimum floor scale and negotiate an amount safely above it
            BigDecimal minFloor = company.getPositionMinimumSalaries().get(position);
            BigDecimal negotiatedSalary = minFloor.add(new BigDecimal(200 + (i * 150)));

            Contract contract = new Contract(company.incrementAndGetContractCounter(), employee, position, negotiatedSalary);
            company.addContract(contract);
            createdContracts.add(contract);
        }
        return createdContracts;
    }

    /**
     * Loops through and registers baseline minimum entry salary parameters across all 9 roles.
     */
    private static void configurePositionFloors(Company company, BigDecimal juniorFloor, BigDecimal seniorFloor, BigDecimal managementFloor) {
        for (Position pos : Position.values()) {
            if (pos == Position.MANAGER || pos == Position.DATABASE_SPECIALIST) {
                company.setSalaryForPosition(pos, managementFloor);
            } else if (pos == Position.SENIOR_DEVELOPER || pos == Position.SYSTEM_ADMINISTRATOR) {
                company.setSalaryForPosition(pos, seniorFloor);
            } else {
                company.setSalaryForPosition(pos, juniorFloor);
            }
        }
    }

    private static List<Contract> filterContractsByPosition(List<Contract> list, Position target) {
        return list.stream().filter(c -> c.getPosition() == target).toList();
    }

    private static List<Contract> filterContractsByOppositePosition(List<Contract> list, Position target) {
        return list.stream().filter(c -> c.getPosition() != target).toList();
    }
}
