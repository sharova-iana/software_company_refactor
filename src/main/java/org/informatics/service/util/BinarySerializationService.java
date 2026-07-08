package org.informatics.service.util;

import java.io.Serializable;

/**
 * Defines the infrastructure contract for serializing and deserializing objects.
 * This engine handles low-level Java object streams to preserve or restore system state.
 */
public interface BinarySerializationService {

    /**
     * Serializes an object into a local binary file.
     *
     * @param filename the absolute target file directory path string on the disk filesystem
     * @param object   the target serializable object instance payload to save
     * @param <T>      the type bound restricted to objects implementing {@link Serializable}
     * @throws RuntimeException if a fatal hardware level input/output write collision occurs
     */
    <T extends Serializable> void serialize(String filename, T object);

    /**
     * Deserializes a binary file back into a live Java object in memory.
     *
     * @param filename    the absolute source file directory path string on the disk filesystem
     * @param targetClass the reflection class mapping target used to safely cast the decoded payload
     * @param <T>         the target type bound restricted to objects implementing {@link Serializable}
     * @return the fully reconstructed, live Java object instance
     * @throws RuntimeException if the file is missing, corrupted, or contains structural format mismatches
     */
    <T extends Serializable> T deserialize(String filename, Class<T> targetClass);
}
