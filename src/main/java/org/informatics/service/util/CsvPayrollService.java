package org.informatics.service.util;

import org.informatics.data.Company;

/**
 * Defines the infrastructure contract for exporting and importing company payroll data using flat CSV text files.
 * This service handles low-level character file streams to facilitate data exchange with external accounting systems.
 */
public interface CsvPayrollService {

    /**
     * Exports the active company contracts and employee compensation records into a formatted CSV text file.
     * The generated file includes structured header markers and handles punctuation escaping automatically.
     *
     * @param company  the active {@link Company} aggregate root instance containing the contract records to save
     * @param filepath the target file path destination on the local filesystem where the CSV file will be written
     * @throws org.informatics.exceptions.FileRegistryException if a hardware-level input/output write error occurs
     * @throws NullPointerException if either the company or the filepath parameter references are null
     */
    void exportPayrollToCsv(Company company, String filepath);

    /**
     * Reads a formatted CSV file and reconstructs the text rows back into live company contract and employee records.
     * This method clears the existing contracts list before rebuilding from the file, while keeping active team records untouched.
     *
     * @param company  the target {@link Company} aggregate root instance where the imported records will be populated
     * @param filepath the source CSV file path destination on the local filesystem to be read and parsed
     * @throws org.informatics.exceptions.FileRegistryException if the requested file cannot be located or opened
     * @throws org.informatics.exceptions.SecurityViolationException if the file's metadata header does not match the active company workspace name
     * @throws org.informatics.exceptions.DataCorruptionException if a row contains malformed data or fails to match the required schema
     * @throws NullPointerException if either the company or the filepath parameter references are null
     */
    void importPayrollFromCsv(Company company, String filepath);
}
