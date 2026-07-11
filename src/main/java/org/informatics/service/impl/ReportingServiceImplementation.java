package org.informatics.service.impl;

import org.informatics.data.Company;
import org.informatics.data.Contract;
import org.informatics.data.Employee;
import org.informatics.service.ReportingService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * <p>Implementation responsible for querying registries and compiling presentation data frames.</p>
 * <p>Following pure Query track guidelines of CQRS, this service performs non-mutating operations
 * with zero hardware console stream side effects.</p>
 */
public class ReportingServiceImplementation implements ReportingService {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String[]> compileEmployeeTableData(Company company) {
        Objects.requireNonNull(company, "Company cannot be null.");
        List<String[]> dataRows = new ArrayList<>();

        company.getContracts().stream()
                .sorted(Comparator.comparingInt(Contract::getContractNumber))
                .forEach(contract -> {
                    Employee emp = contract.getEmployee();
                    // Positions and salaries are now collected directly from the active legal agreement layout
                    dataRows.add(new String[]{
                            String.valueOf(contract.getContractNumber()),
                            emp.getId().toString(),
                            emp.getName(),
                            emp.getEmail(),
                            emp.getGender().name(),
                            emp.getBirthDate().toString(),
                            contract.getPosition().name(),
                            String.format("%.2f", contract.getSalary())
                    });
                });
        return dataRows;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String[]> compileTeamTableData(Company company) {
        Objects.requireNonNull(company, "Company cannot be null.");
        List<String[]> dataRows = new ArrayList<>();

        company.getTeams().forEach(team -> {
            Contract managerContract = team.getManagerContract();
            dataRows.add(new String[]{
                    team.getId().toString(),
                    "LEADER: " + managerContract.getEmployee().getName(),
                    managerContract.getPosition().name()
            });

            // Iterating over the contract-centric team membership sets
            if (team.getMemberContracts().isEmpty()) {
                dataRows.add(new String[]{"", "  (No regular member contributors assigned yet)", ""});
            } else {
                team.getMemberContracts().forEach(memberContract -> dataRows.add(new String[]{
                        "",
                        "  - " + memberContract.getEmployee().getName(),
                        memberContract.getPosition().name()
                }));
            }
            dataRows.add(new String[]{"", "", ""});
        });
        return dataRows;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String[]> compileBaseSalariesTableData(Company company) {
        Objects.requireNonNull(company, "Company cannot be null.");
        List<String[]> dataRows = new ArrayList<>();

        company.getPositionMinimumSalaries().forEach((position, salary) -> dataRows.add(new String[]{
                position.name(),
                String.format("%.2f", salary)
        }));
        return dataRows;
    }
}
