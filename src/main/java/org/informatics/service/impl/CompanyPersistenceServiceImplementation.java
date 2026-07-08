package org.informatics.service.impl;

import org.informatics.data.Company;
import org.informatics.service.CompanyPersistenceService;
import org.informatics.service.util.BinarySerializationService;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation responsible for orchestrating company workspace disk operations.
 * Coordinates input/output streams and writes tracking audit logs to the log stream.
 */
public class CompanyPersistenceServiceImplementation implements CompanyPersistenceService {

    private static final Logger LOGGER = Logger.getLogger(CompanyPersistenceServiceImplementation.class.getName());
    private final BinarySerializationService serializationService;

    /**
     * Constructs a new CompanyPersistenceServiceImplementation injecting its infrastructure binary file collaborator.
     *
     * @param serializationService the infrastructure binary serialization engine utility service
     */
    public CompanyPersistenceServiceImplementation(BinarySerializationService serializationService) {
        this.serializationService = Objects.requireNonNull(serializationService, "Serialization service cannot be null.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveWorkspace(Company company, String filepath) {
        Objects.requireNonNull(company, "Company workspace reference cannot be null.");
        Objects.requireNonNull(filepath, "Backup target filepath cannot be null.");

        serializationService.serialize(filepath, company);

        // Background Audit Log Entry
        LOGGER.log(Level.INFO, "Company workspace aggregate successfully serialized. Name: ''{0}'', Target File: {1}",
                new Object[]{company.getName(), filepath});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Company loadWorkspace(String filepath) {
        Objects.requireNonNull(filepath, "Backup source filepath cannot be null.");

        Company restoredCompany = serializationService.deserialize(filepath, Company.class);

        // Background Audit Log Entry
        LOGGER.log(Level.INFO, "Company workspace aggregate successfully deserialized. Restored Name: ''{0}'', Source File: {1}",
                new Object[]{restoredCompany.getName(), filepath});

        return restoredCompany;
    }
}
