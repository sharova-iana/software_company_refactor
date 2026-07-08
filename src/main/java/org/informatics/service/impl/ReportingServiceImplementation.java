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
 * Implementation responsible for querying registries and compiling presentation data.
 * Following pure Query track guidelines, this service performs non-mutating operations
 * with zero hardware console stream side effects.
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

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String[]> compileTeamTableData(Company company) {
        Objects.requireNonNull(company, "Company cannot be null.");
        List<String[]> dataRows = new ArrayList<>();

        company.getTeams().forEach(team -> {
            dataRows.add(new String[]{
                    team.getId().toString(),
                    "LEADER: " + team.getManager().getName(),
                    team.getManager().getPosition().name()
            });

            if (team.getMembers().isEmpty()) {
                dataRows.add(new String[]{"", "  (No regular member contributors assigned yet)", ""});
            } else {
                team.getMembers().forEach(member -> dataRows.add(new String[]{
                        "",
                        "  - " + member.getName(),
                        member.getPosition().name()
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
