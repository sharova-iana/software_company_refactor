package org.informatics.service.impl;

import org.informatics.data.Company;
import org.informatics.data.Contract;
import org.informatics.data.Employee;
import org.informatics.data.Team;
import org.informatics.data.enums.Gender;
import org.informatics.data.enums.Position;
import org.informatics.service.CompanyService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class CompanyServiceImplementation implements CompanyService {

    // 1. Set the minimum salary for a specific position
    @Override
    public void setSalaryForPosition(Company company, Position position, BigDecimal minSalary) {
        Objects.requireNonNull(company, "Company cannot be null.");
        Objects.requireNonNull(position, "Position cannot be null.");
        Objects.requireNonNull(minSalary, "Salary cannot be null.");
        // Maps to: InvalidSalaryException
        if (minSalary.compareTo(BigDecimal.ZERO) < 0) {
            throw new org.informatics.exceptions.InvalidSalaryException("Minimum salary floor cannot be negative.");
        }
        company.getPositionMinimumSalaries().put(position, minSalary);
    }

    // Public because it is a validation check (no data mutation), but left out of the interface contract
    public boolean isAgeValid(LocalDate birthDate) {
        java.util.Objects.requireNonNull(birthDate, "Birth date cannot be null.");

        // Calculate chronological age relative to the current execution year (2026)
        int age = java.time.Period.between(birthDate, java.time.LocalDate.now()).getYears();

        // Enforce age boundaries check (18 to 70 years old inclusive)
        // Maps to: AgeBoundaryException
        if (age < 18 || age > 70) {
            throw new org.informatics.exceptions.AgeBoundaryException(
                    String.format("Hiring rejected. Age must be between 18 and 70. Provided birth date results in age: %d.", age));
        }

        return true;
    }


    // 2. Hire an employee to the company
    @Override
    public Employee hireEmployee(Company company, String name, Gender gender, LocalDate birthDate, Position position, BigDecimal negotiatedSalary) {
        Objects.requireNonNull(company, "Company cannot be null.");
        Objects.requireNonNull(negotiatedSalary, "Negotiated salary cannot be null.");

        // Delegates internally to trigger AgeBoundaryException
        this.isAgeValid(birthDate);

        BigDecimal minFloor = company.getPositionMinimumSalaries().get(position);

        // Maps to: SalaryConfigurationException
        if (minFloor == null) {
            throw new org.informatics.exceptions.SalaryConfigurationException(
                    "Cannot hire. No entry-level salary baseline configured for position: " + position);
        }

        // Maps to: InvalidSalaryException
        if (negotiatedSalary.compareTo(minFloor) < 0) {
            throw new org.informatics.exceptions.InvalidSalaryException(String.format(
                    "Rejected: Proposed salary %s is below the minimum baseline entry bound of %s for a %s.",
                    negotiatedSalary, minFloor, position));
        }


        // Pass the unique negotiated salary to the Employee constructor
        Employee employee = new Employee(name, gender, birthDate, position, negotiatedSalary);

        int contractNumber = company.incrementAndGetContractCounter();
        Contract contract = new Contract(contractNumber, employee);
        company.getContracts().add(contract);

        return employee;
    }


    // =========================================================================
    // Standalone Public Lookup Helpers (Indirectly used by 6, 7, 8, 9)
    // =========================================================================

    public Employee findEmployeeById(Company company, UUID employeeId) {
        java.util.Objects.requireNonNull(company, "Company cannot be null.");
        java.util.Objects.requireNonNull(employeeId, "Employee ID cannot be null.");

        // Maps to: EntityNotFoundException
        return company.getContracts().stream()
                .map(Contract::getEmployee)
                .filter(emp -> emp.getId().equals(employeeId))
                .findFirst()
                .orElseThrow(() -> new org.informatics.exceptions.EntityNotFoundException(
                        "No employee found with ID: " + employeeId));
    }

    public Team findTeamById(Company company, UUID teamId) {
        java.util.Objects.requireNonNull(company, "Company cannot be null.");
        java.util.Objects.requireNonNull(teamId, "Team ID cannot be null.");

        // Maps to: EntityNotFoundException
        return company.getTeams().stream()
                .filter(team -> team.getId().equals(teamId))
                .findFirst()
                .orElseThrow(() -> new org.informatics.exceptions.EntityNotFoundException(
                        "No team found with ID: " + teamId));
    }

    public Contract findContractByEmployeeId(Company company, UUID employeeId) {
        java.util.Objects.requireNonNull(company, "Company cannot be null.");
        java.util.Objects.requireNonNull(employeeId, "Employee ID cannot be null.");

        // Maps to: EntityNotFoundException
        return company.getContracts().stream()
                .filter(contract -> contract.getEmployee().getId().equals(employeeId))
                .findFirst()
                .orElseThrow(() -> new org.informatics.exceptions.EntityNotFoundException(
                        "No active employment contract found for Employee ID: " + employeeId));
    }

    // 3. Terminate an employee from the company
    @Override
    public boolean fireEmployee(Company company, UUID employeeId) {
        // Use lookup helper. If the contract doesn't exist,
        // it immediately throws a EntityNotFoundException, stopping execution.
        Contract targetContract = this.findContractByEmployeeId(company, employeeId);

        // Action 1: Remove the active contract from company records
        company.getContracts().remove(targetContract);

        // Action 2: Clean up assignments across all existing teams
        removeEmployeeFromAllTeams(company, employeeId);

        // Action 3: Purge teams that no longer have a manager
        purgeManagerlessTeams(company);

        return true;
    }


    // Private because it directly mutates collections/data
    /**
     * Helper method to scrub an employee from both manager positions
     * and member sets across all active company teams.
     */
    private void removeEmployeeFromAllTeams(Company company, UUID employeeId) {
        company.getTeams().forEach(team -> {
            // If they are the manager, strip the title
            if (team.getManager() != null && team.getManager().getId().equals(employeeId)) {
                team.setManager(null);
            }
            // If they are a member, evict them from the member set
            team.getMembers().removeIf(member -> member.getId().equals(employeeId));
        });
    }

    // Private because it directly mutates collections/data
    /**
     * Helper method to automatically dissolve and remove any teams
     * from the company registry that have lost their manager.
     */
    private void purgeManagerlessTeams(Company company) {
        company.getTeams().removeIf(team -> team.getManager() == null);
    }

    // 4. Display the complete list of employees
    @Override
    public void displayAllEmployees(Company company) {
        java.util.Objects.requireNonNull(company, "Company cannot be null.");

        System.out.println("\n=== Active Employees Registry for " + company.getName() + " ===");

        String[] headers = {"Contract #", "Employee ID", "Full Name", "Gender", "Birth Date", "Position", "Salary"};

        // Delegates the mapping, sorting, and text conversion tasks
        java.util.List<String[]> dataRows = convertContractsToTableRows(company);

        // Render the final matrix grid cleanly using the table engine
        org.informatics.ui.util.TableFormatter.printTable(headers, dataRows);
    }

    /**
     * Public non-mutating helper: Transforms the company's active contract set
     * into a sequentially sorted list of flat string arrays for tabular formatting.
     */
    public java.util.List<String[]> convertContractsToTableRows(Company company) {
        java.util.Objects.requireNonNull(company, "Company cannot be null.");

        java.util.List<String[]> dataRows = new java.util.ArrayList<>();

        company.getContracts().stream()
                .sorted(java.util.Comparator.comparingInt(Contract::getContractNumber))
                .forEach(contract -> {
                    Employee emp = contract.getEmployee();
                    dataRows.add(new String[]{
                            String.valueOf(contract.getContractNumber()),
                            emp.getId().toString(),
                            emp.getName(),
                            emp.getGender().name(),
                            emp.getBirthDate().toString(),
                            emp.getPosition().name(),
                            String.format("%.2f", emp.getSalary())
                    });
                });

        return dataRows;
    }

    // 5. Return the total count of employees earning strictly more than a given threshold
    @Override
    public long countEmployeesWithSalaryGreaterThan(Company company, BigDecimal threshold) {
        Objects.requireNonNull(company, "Company cannot be null.");
        Objects.requireNonNull(threshold, "Threshold cannot be null.");

        return company.getContracts().stream()
                .map(Contract::getEmployee)
                .filter(employee -> employee.getSalary().compareTo(threshold) > 0)
                .count();
    }


    // =========================================================================
    // CORE BUSINESS OPERATIONS (Methods 6 - 9)
    // =========================================================================

    // 6. Instantiate a new Team led by a verified manager
    @Override
    public Team createTeam(Company company, UUID managerEmployeeId) {
        // Use helper to locate employee (automatically throws exception if missing)
        Employee employee = this.findEmployeeById(company, managerEmployeeId);

        // Enforce Rule: The employee must have the MANAGER position enum
        if (employee.getPosition() != Position.MANAGER) {
            throw new org.informatics.exceptions.PositionMismatchException(
                    employee.getName(), Position.MANAGER, employee.getPosition());
        }

        // Enforce Rule: A manager can lead only one team
        boolean isAlreadyManaging = company.getTeams().stream()
                .anyMatch(team -> team.getManager() != null && team.getManager().equals(employee));

        if (isAlreadyManaging) {
            throw new org.informatics.exceptions.TeamAssignmentException(
                    "Manager '" + employee.getName() + "' is already leading another team.");
        }

        Team newTeam = new Team(employee);
        company.getTeams().add(newTeam);
        return newTeam;
    }

    // 7. Add hired employee to a team (with single-team constraint verification)
    @Override
    public boolean addMemberToTeam(Company company, UUID teamId, UUID employeeId) {
        // Use helpers to locate target components cleanly
        Team targetTeam = this.findTeamById(company, teamId);
        Employee employee = this.findEmployeeById(company, employeeId);

        // Enforce Rule: Employees with the MANAGER role cannot be added as standard contributor members
        if (employee.getPosition() == Position.MANAGER) {
            throw new org.informatics.exceptions.TeamAssignmentException(
                    "Security Block: Employee '" + employee.getName() + "' holds a MANAGER role " +
                            "and cannot be assigned as a standard member contributor.");
        }

        // Enforce Rule: An employee can only belong to one single team at a time across the company
        boolean alreadyInAnyTeam = company.getTeams().stream()
                .anyMatch(team -> team.getMembers().stream().anyMatch(member -> member.equals(employee)));

        if (alreadyInAnyTeam) {
            throw new org.informatics.exceptions.TeamAssignmentException(
                    "Employee '" + employee.getName() + "' is already assigned to a team in this company.");
        }

        return targetTeam.getMembers().add(employee);
    }

    // 8. Dissolve a team by removing its manager
    @Override
    public boolean removeManagerFromTeam(Company company, UUID teamId) {
        // Use helper to locate the target team.
        // If the team does not exist, it will immediately throw a EntityNotFoundException.
        Team targetTeam = this.findTeamById(company, teamId);

        // Since a team cannot logically exist without a manager,
        // we completely remove the team structure from the company registry.
        return company.getTeams().remove(targetTeam);
    }


    // 9. Evict a member from a specific team
    @Override
    public boolean removeMemberFromTeam(Company company, UUID teamId, UUID employeeId) {
        Objects.requireNonNull(employeeId, "Employee ID cannot be null.");

        // Find the target team and employee using the public helper
        Team targetTeam = this.findTeamById(company, teamId);
        Employee employee = this.findEmployeeById(company, employeeId);

        // Remove the member match inside the team set
        return targetTeam.getMembers().removeIf(member -> member.equals(employee));
    }


    // 10. Display complete company organizational structure
    @Override
    public void displayAllTeams(Company company) {
        java.util.Objects.requireNonNull(company, "Company cannot be null.");

        System.out.println("\n=== Corporate Teams Directory for " + company.getName() + " ===");

        String[] headers = {"Team Identifier Token", "Assigned Leader / Contributor Pool Track", "Professional Role Tier"};

        // Delegates the complex relational nesting and sorting transformations
        java.util.List<String[]> dataRows = convertTeamsToTableRows(company);

        // Render the complete nested directory grid cleanly using our table engine
        org.informatics.ui.util.TableFormatter.printTable(headers, dataRows);
    }

    /**
     * Public non-mutating helper: Transforms the company's active team collection
     * matrix into a structured list of text arrays for clean tabular formatting.
     */
    public java.util.List<String[]> convertTeamsToTableRows(Company company) {
        java.util.Objects.requireNonNull(company, "Company cannot be null.");

        java.util.List<String[]> dataRows = new java.util.ArrayList<>();

        company.getTeams().forEach(team -> {
            // Row A: Print the Primary Team ID and its assigned Manager Leader profile
            dataRows.add(new String[]{
                    team.getId().toString(),
                    "LEADER: " + team.getManager().getName(),
                    team.getManager().getPosition().name()
            });

            // Row B: Loop through and print individual member rows underneath
            if (team.getMembers().isEmpty()) {
                dataRows.add(new String[]{
                        "",
                        "  (No regular member contributors assigned yet)",
                        ""
                });
            } else {
                team.getMembers().forEach(member -> {
                    dataRows.add(new String[]{
                            "", // Leave column 1 blank to visually group items under the same team header
                            "  - " + member.getName(),
                            member.getPosition().name()
                    });
                });
            }

            // Row C: Empty spacing buffer row to provide clean separation between team blocks
            dataRows.add(new String[]{"", "", ""});
        });

        return dataRows;
    }

    // 11. Dynamically calculate precise average salary for any position using Stream API
    @Override
    public BigDecimal calculateAverageSalaryForPosition(Company company, Position position) {
        Objects.requireNonNull(company, "Company cannot be null.");
        Objects.requireNonNull(position, "Position cannot be null.");

        // Collect all employees matching the requested position tier
        var matchingEmployees = company.getContracts().stream()
                .map(Contract::getEmployee)
                .filter(emp -> emp.getPosition() == position)
                .toList();

        if (matchingEmployees.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Accumulate salaries with exact precision using standard big decimal reduction
        BigDecimal totalSalary = matchingEmployees.stream()
                .map(Employee::getSalary)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalSalary.divide(BigDecimal.valueOf(matchingEmployees.size()), 2, java.math.RoundingMode.HALF_UP);
    }

    // 12. Display position salary floors
    @Override
    public void displayBaseSalaries(Company company) {
        java.util.Objects.requireNonNull(company, "Company cannot be null.");

        System.out.println("\n=== Base Salaries Configuration Table for " + company.getName() + " ===");

        String[] headers = {"Corporate Position Tier Constant", "Configured Minimum Base Floor Salary"};

        // Delegates the map configuration translation task
        java.util.List<String[]> dataRows = convertBaseSalariesToTableRows(company);

        // Render the final matrix grid cleanly using our table engine
        org.informatics.ui.util.TableFormatter.printTable(headers, dataRows);
    }

    /**
     * Public non-mutating helper: Transforms the company's internal position
     * salary minimum floor map entries into a flat list of text arrays for formatting.
     */
    public java.util.List<String[]> convertBaseSalariesToTableRows(Company company) {
        java.util.Objects.requireNonNull(company, "Company cannot be null.");

        java.util.List<String[]> dataRows = new java.util.ArrayList<>();

        company.getPositionMinimumSalaries().forEach((position, salary) -> {
            dataRows.add(new String[]{
                    position.name(),
                    String.format("%.2f", salary)
            });
        });

        return dataRows;
    }

}

