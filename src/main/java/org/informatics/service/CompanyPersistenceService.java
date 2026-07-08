package org.informatics.service;

import org.informatics.data.Company;

/**
 * Defines the contract for orchestrating complete company workspace state saves and restorations.
 * This orchestrator acts as the clean intermediary layer between the user interface and binary disk utilities.
 */
public interface CompanyPersistenceService {

    /**
     * Saves the entire company workspace aggregate state into a designated backup file.
     *
     * @param company  the active {@link Company} aggregate root instance targeted for serialization
     * @param filepath the target file directory destination path string on the disk filesystem
     */
    void saveWorkspace(Company company, String filepath);

    /**
     * Restores a previously saved company workspace aggregate state graph back into memory.
     *
     * @param filepath the source backup file directory destination path string on the disk filesystem
     * @return the completely reconstructed, live {@link Company} aggregate root state graph
     */
    Company loadWorkspace(String filepath);
}
