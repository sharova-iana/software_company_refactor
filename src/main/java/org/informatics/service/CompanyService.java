package org.informatics.service;

import org.informatics.data.Company;
import org.informatics.data.Employee;
import org.informatics.data.Team;
import org.informatics.data.enums.Gender;
import org.informatics.data.enums.Position;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface CompanyService {

    // 1. Set position salary
    void setSalaryForPosition(Company company, Position position, BigDecimal salary);

    // 2. Hire an employee to the company with automated UUID and Contract generation
    Employee hireEmployee(Company company, String name, Gender gender, LocalDate birthDate, Position position, BigDecimal negotiatedSalary);

    // 3. Fire employee from company and auto-evict them from any team assignments
    boolean fireEmployee(Company company, UUID employeeId);

    // 4. Print all employees to console using Stream API
    void displayAllEmployees(Company company);

    // 5. Count workers earning strictly above threshold using Stream API
    long countEmployeesWithSalaryGreaterThan(Company company, BigDecimal threshold);

    // 6. Instantiate a new Team led by a verified manager
    Team createTeam(Company company, UUID managerEmployeeId);

    // 7. Add hired employee to a team
    boolean addMemberToTeam(Company company, UUID teamId, UUID employeeId);

    // 8. Dissolve a team by removing its manager
    boolean removeManagerFromTeam(Company company, UUID teamId);

    // 9. Evict a member from a team
    boolean removeMemberFromTeam(Company company, UUID teamId, UUID employeeId);

    // 10. Display complete company organizational structure
    void displayAllTeams(Company company);

    // 11. Dynamically calculate precise average salary for any position using Stream API
    BigDecimal calculateAverageSalaryForPosition(Company company, Position position);

    // 12. Display position salary floors
    void displayBaseSalaries(Company company);
}
