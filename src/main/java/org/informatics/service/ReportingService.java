package org.informatics.service;

import org.informatics.data.Company;
import java.util.List;

/**
 * Defines the contract for compiling data registries into raw tabular matrix frames.
 * Following the Query track of CQRS, these operations are strictly read-only and return
 * presentation-ready data blocks without interacting with any hardware output streams.
 */
public interface ReportingService {

    /**
     * Compiles the company's active contract set into flat text rows for personnel tables.
     *
     * @param company the target company workspace containing the contracts registry
     * @return a sequentially sorted list of string rows ready for presentation rendering
     */
    List<String[]> compileEmployeeTableData(Company company);

    /**
     * Compiles established team components and deep member sets into grouped row frames.
     *
     * @param company the target company workspace containing the teams registry
     * @return a structured list of multi-line team blocks ready for presentation rendering
     */
    List<String[]> compileTeamTableData(Company company);

    /**
     * Compiles position minimum configuration map pairs into clear list rows.
     *
     * @param company the target company workspace containing the configurations
     * @return a flat list of salary mapping rows ready for presentation rendering
     */
    List<String[]> compileBaseSalariesTableData(Company company);
}
