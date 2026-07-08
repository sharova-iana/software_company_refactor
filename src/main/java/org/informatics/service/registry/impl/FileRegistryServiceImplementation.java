package org.informatics.service.registry.impl;

import org.informatics.service.registry.FileRegistryService;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class FileRegistryServiceImplementation implements FileRegistryService {

    /**
     * {@inheritDoc}
     */
    @Override
    public File[] fetchRawSavedFiles() {
        File currentFolder = new File(".");
        File[] files = currentFolder.listFiles((dir, name) ->
                name.startsWith("company_db_") && name.endsWith(".ser")
        );
        return files != null ? files : new File[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String parseFilenameToCompanyName(String filename) {
        Objects.requireNonNull(filename, "Filename cannot be null.");
        // Maps to: FileRegistryException
        if (!filename.startsWith("company_db_") || !filename.endsWith(".ser")) {
            throw new org.informatics.exceptions.FileRegistryException("Invalid data format file marker: " + filename);
        }

        String coreName = filename.substring("company_db_".length(), filename.length() - ".ser".length());
        String[] words = coreName.split("_");

        StringBuilder cleanNameBuilder = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (!words[i].isEmpty()) {
                cleanNameBuilder.append(Character.toUpperCase(words[i].charAt(0)))
                        .append(words[i].substring(1));
                if (i < words.length - 1) {
                    cleanNameBuilder.append(" ");
                }
            }
        }
        return cleanNameBuilder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> scanAvailableSavedCompanies() {
        File[] savedFiles = fetchRawSavedFiles();
        Set<String> companyNames = new LinkedHashSet<>();

        for (File file : savedFiles) {
            String cleanName = parseFilenameToCompanyName(file.getName());
            companyNames.add(cleanName);
        }
        return companyNames;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generateFilename(String companyName) {
        Objects.requireNonNull(companyName, "Company name cannot be null.");
        String safeName = companyName.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
        return "company_db_" + safeName + ".ser";
    }
}