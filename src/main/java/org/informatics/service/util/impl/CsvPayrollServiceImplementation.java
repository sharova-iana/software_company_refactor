package org.informatics.service.util.impl;

import org.informatics.data.Company;
import org.informatics.data.Contract;
import org.informatics.data.Employee;
import org.informatics.data.enums.Gender;
import org.informatics.data.enums.Position;
import org.informatics.service.util.CsvPayrollService;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class CsvPayrollServiceImplementation implements CsvPayrollService {

    private static final String CSV_HEADER = "ContractNumber,EmployeeID,FullName,Gender,BirthDate,Position,Salary";
    private static final String COMPANY_METADATA_PREFIX = "# COMPANY_NAME: ";

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportPayrollToCsv(Company company, String filepath) {
        Objects.requireNonNull(company, "Company cannot be null.");
        Objects.requireNonNull(filepath, "Filepath cannot be null.");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
            // Write Row 1: Explicitly imbed the workspace company metadata tag context
            writer.write(COMPANY_METADATA_PREFIX + company.getName());
            writer.newLine();

            // Write Row 2: Standard structural header fields
            writer.write(CSV_HEADER);
            writer.newLine();

            // Stream and format every single active contract entry
            for (Contract contract : company.getContracts()) {
                String line = getString(contract);

                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("CSV Export failed for path: " + filepath, e);
        }
    }

    public String getString(Contract contract) {
        Employee emp = contract.getEmployee();

        // Sanitize string text parameters to escape internal comma breaking errors
        String sanitizedName = "\"" + emp.getName().replace("\"", "\"\"") + "\"";

        return String.format("%d,%s,%s,%s,%s,%s,%s",
                contract.getContractNumber(),
                emp.getId().toString(),
                sanitizedName,
                emp.getGender().name(),
                emp.getBirthDate().toString(),
                emp.getPosition().name(),
                emp.getSalary().toString()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void importPayrollFromCsv(Company company, String filepath) {
        Objects.requireNonNull(company, "Company cannot be null.");
        Objects.requireNonNull(filepath, "Filepath cannot be null.");

        File file = new File(filepath);
        // Maps to: FileRegistryException
        if (!file.exists()) {
            throw new org.informatics.exceptions.FileRegistryException("CSV Import failed. File not found: " + filepath);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            // Helper A: Handles row 1 company ownership verification rules
            verifyCompanyOwnershipMetadata(company.getName(), reader.readLine());

            // Helper B: Handles row 2 schema structure layout validations
            verifyCsvHeaderSchema(reader.readLine());

            // Safety clearance step before populating new data rows
            company.clearContractsRegistry();

            // Helper C: Handles row parsing and roster reconstruction loop
            processCsvDataLines(company, reader);

        } catch (IOException e) {
            throw new RuntimeException("CSV Import read loop failure: " + filepath, e);
        }
    }

    // Public because it validates a read stream state (left out of the interface)
    public void verifyCompanyOwnershipMetadata(String currentCompanyName, String metadataLine) {
        // Maps to: DataCorruptionException (Line 1 is physically corrupt or missing)
        if (metadataLine == null || !metadataLine.startsWith(COMPANY_METADATA_PREFIX)) {
            throw new org.informatics.exceptions.DataCorruptionException("CSV Corruption error: Missing corporate ownership metadata header.");
        }

        // Maps to: SecurityViolationException (Cross-loading files from an unauthorized competitor)
        String embeddedCompanyName = metadataLine.substring(COMPANY_METADATA_PREFIX.length()).trim();
        if (!embeddedCompanyName.equalsIgnoreCase(currentCompanyName)) {
            throw new org.informatics.exceptions.SecurityViolationException(String.format(
                    "Security Rejection: Cannot import data roster. The chosen file belongs to company '%s', " +
                            "but the current active workspace profile is configured for '%s'.",
                    embeddedCompanyName, currentCompanyName));
        }
    }

    // Public because it validates a layout header token array string (left out of the interface)
    public void verifyCsvHeaderSchema(String headerLine) {
        // Maps to: DataCorruptionException
        if (headerLine == null || !headerLine.equals(CSV_HEADER)) {
            throw new org.informatics.exceptions.DataCorruptionException("CSV Corruption error: Invalid or missing data header schema marker.");
        }
    }

    // Private because it directly mutates the company collection records data layer
    private void processCsvDataLines(Company company, BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;

            String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            // Maps to: DataCorruptionException
            if (tokens.length != 7) {
                throw new org.informatics.exceptions.DataCorruptionException("CSV Data mismatch corruption error. Row token length mismatch.");
            }

            int contractNum = Integer.parseInt(tokens[0].trim());
            UUID empId = UUID.fromString(tokens[1].trim());

            Employee employee = buildEmployeeFromTokens(tokens);
            overrideEmployeeId(employee, empId);

            Contract contract = new Contract(contractNum, employee);
            company.addContract(contract);

            if (contractNum > company.getContractCounter()) {
                company.setContractCounter(contractNum);
            }
        }
    }


    public Employee buildEmployeeFromTokens(String[] tokens) {
        String cleanName = tokens[2].trim().replaceAll("^\"|\"$", "").replace("\"\"", "\"");
        Gender gender = Gender.valueOf(tokens[3].trim().toUpperCase());
        LocalDate birthDate = LocalDate.parse(tokens[4].trim());
        Position position = Position.valueOf(tokens[5].trim().toUpperCase());
        BigDecimal salary = new BigDecimal(tokens[6].trim());

        return new Employee(cleanName, gender, birthDate, position, salary);
    }

    public void overrideEmployeeId(Employee employee, UUID targetId) {
        try {
            java.lang.reflect.Field idField = Employee.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(employee, targetId);
        } catch (Exception e) {
            throw new RuntimeException("Critical reflective injection error processing data schemas", e);
        }
    }
}