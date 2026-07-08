package org.informatics.service.registry;

import java.io.File;
import java.util.Set;

/**
 * Defines the workspace registry contract for managing, scanning, and resolving
 * localized backup database file pointers on the filesystem.
 */
public interface FileRegistryService {

    /**
     * Scans the application's root execution directory, parses matching database tokens,
     * and compiles a unique set of clean company workspace display titles.
     * <p>The returned set preserves the deterministic lookup sequence of the underlying files
     * discovered on the local filesystem.</p>
     *
     * @return a {@link Set} of sanitized company title strings available for loading
     */
    Set<String> scanAvailableSavedCompanies();


    /**
     * Scans the application's local workspace directory to fetch raw file pointer handles
     * matching the corporate serialization storage schema.
     *
     * @return an array of {@link File} objects representing the active serialized database backups discovered
     */
    File[] fetchRawSavedFiles();

    /**
     * Translates a raw system database backup filename string into a formatted, capitalized,
     * and properly spaced company workspace display title.
     *
     * @param filename the raw source backup file name string discovered on the disk filesystem
     * @return the completely reconstructed, human-readable company workspace name string
     * @throws org.informatics.exceptions.FileRegistryException if the filename formatting string violates the mandatory tracking signature rules
     * @throws NullPointerException if the provided filename parameter reference is null
     */
    String parseFilenameToCompanyName(String filename);

    /**
     * Computes a standardized, safe, and lowercase binary storage filename from a raw company workspace name.
     * Special alphanumeric symbols and spaces are safely converted into uniform underscore separators.
     *
     * @param companyName the raw company title string used to generate the storage key
     * @return the standardized system file storage string with the correct file extension appended
     * @throws NullPointerException if the provided companyName parameter reference is null
     */
    String generateFilename(String companyName);
}
