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

/**
 * <p>Provides the concrete infrastructure implementation for exporting and importing corporate payroll files.</p>
 * <p>Following our contract-centric domain shift, this class manages a strict 8-column schema, separating human
 * identity profiles from their legal agreement terms during plain text file parses.</p>
 */
public class CsvPayrollServiceImplementation implements CsvPayrollService {

    // Upgraded to a strict 8-column layout mapping out the human email string parameter
    private static final String CSV_HEADER = "ContractNumber,EmployeeID,FullName,Email,Gender,BirthDate,Position,Salary";
    private static final String COMPANY_METADATA_PREFIX = "# COMPANY_NAME: ";

    /**
     * {@inheritDoc}
     */
    @Override
    public void exportPayrollToCsv(Company company, String filepath) {
        Objects.requireNonNull(company, "Company cannot be null.");
        Objects.requireNonNull(filepath, "Filepath cannot be null.");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
            // Write Row 1: Embed the workspace company metadata tag context
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
            throw new org.informatics.exceptions.FileRegistryException("CSV Export failed for path: " + filepath);
        }
    }

    /**
     * Translates a live contract and its nested employee details into a sanitized, comma-separated row text string.
     *
     * @param contract the live contract agreement entity to stringify
     * @return the formatted plain text CSV string data line
     */
    public String getString(Contract contract) {
        Employee emp = contract.getEmployee();

        // Sanitize string text parameters to escape internal comma breaking errors
        String sanitizedName = "\"" + emp.getName().replace("\"", "\"\"") + "\"";

        // Upgraded formatting signature tracking 8 parameters sequentially
        return String.format("%d,%s,%s,%s,%s,%s,%s,%s",
                contract.getContractNumber(),
                emp.getId().toString(),
                sanitizedName,
                emp.getEmail(),
                emp.getGender().name(),
                emp.getBirthDate().toString(),
                contract.getPosition().name(),
                contract.getSalary().toString()
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
        if (!file.exists()) {
            throw new org.informatics.exceptions.FileRegistryException("CSV Import failed. File not found: " + filepath);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            // Handles row 1 company ownership verification rules
            verifyCompanyOwnershipMetadata(company.getName(), reader.readLine());

            // Handles row 2 schema structure layout validations
            verifyCsvHeaderSchema(reader.readLine());

            // Safety clearance step before populating new data rows
            company.clearContractsRegistry();

            // Handles row parsing and roster reconstruction loop
            processCsvDataLines(company, reader);

        } catch (IOException e) {
            throw new org.informatics.exceptions.FileRegistryException("CSV Import read loop failure: " + filepath);
        }
    }

    /**
     * Validates that the metadata header owner token matches the current workspace title precisely.
     *
     * @param currentCompanyName the title string of the active workspace profile
     * @param metadataLine       the raw header text read off the first line of the file
     */
    public void verifyCompanyOwnershipMetadata(String currentCompanyName, String metadataLine) {
        if (metadataLine == null || !metadataLine.startsWith(COMPANY_METADATA_PREFIX)) {
            throw new org.informatics.exceptions.DataCorruptionException("CSV Corruption error: Missing corporate ownership metadata header.");
        }

        String embeddedCompanyName = metadataLine.substring(COMPANY_METADATA_PREFIX.length()).trim();
        if (!embeddedCompanyName.equalsIgnoreCase(currentCompanyName)) {
            throw new org.informatics.exceptions.SecurityViolationException(String.format(
                    "Security Rejection: Cannot import data roster. The chosen file belongs to company '%s', " +
                            "but the current active workspace profile is configured for '%s'.",
                    embeddedCompanyName, currentCompanyName));
        }
    }

    /**
     * Validates that the file layout column header string matches our structural 8-column layout contract.
     *
     * @param headerLine the layout title text read off the second line of the file
     */
    public void verifyCsvHeaderSchema(String headerLine) {
        if (headerLine == null || !headerLine.equals(CSV_HEADER)) {
            throw new org.informatics.exceptions.DataCorruptionException("CSV Corruption error: Invalid or missing data header schema marker.");
        }
    }

    /**
     * Iterates over remaining lines, splitting string tokens safely and reconstructing live domain models.
     */
    private void processCsvDataLines(Company company, BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;

            // Regex parsing that isolates escaped internal commas cleanly
            String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            if (tokens.length != 8) {
                throw new org.informatics.exceptions.DataCorruptionException("CSV Data mismatch corruption error. Row token length mismatch.");
            }

            int contractNum = Integer.parseInt(tokens[0].trim());

            // 1. Re-hydrate the human employee model using our clean parameters natively in a single step
            Employee employee = buildEmployeeFromTokens(tokens);

            // 2. Extract position and salary parameters to register the contract model
            Position position = Position.valueOf(tokens[6].trim().toUpperCase());
            BigDecimal salary = new BigDecimal(tokens[7].trim());

            Contract contract = new Contract(contractNum, employee, position, salary);
            company.addContract(contract);

            if (contractNum > company.getContractCounter()) {
                company.setContractCounter(contractNum);
            }
        }
    }

    /**
     * Extracts values from string arrays to allocate a fresh, compile-checked Employee instance.
     *
     * @param tokens       the parsed raw string columns array row
     * @return a fresh, verified Employee domain instance
     */
    public Employee buildEmployeeFromTokens(String[] tokens) {
        UUID empId = UUID.fromString(tokens[1].trim());
        String cleanName = tokens[2].trim().replaceAll("^\"|\"$", "").replace("\"\"", "\"");
        String email = tokens[3].trim();
        Gender gender = Gender.valueOf(tokens[4].trim().toUpperCase());
        LocalDate birthDate = LocalDate.parse(tokens[5].trim());

        // Safe compile-checked instantiation line replacing reflection backdoors completely
        return new Employee(empId, cleanName, email, gender, birthDate);
    }
}
