package org.informatics.exceptions;

import java.io.Serial;

/**
 * <p>Thrown when an error occurs during file storage or local disk input/output operations.</p>
 * <p>This exception is typically triggered when the system cannot find a requested saved file,
 * fails to access the file directory, or encounters a hardware-level read/write restriction
 * while serializing or deserializing company records.</p>
 */
public class FileRegistryException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -2543676094946088274L;

    /**
     * <p>Constructs a new FileRegistryException with the specified descriptive error message.</p>
     *
     * @param message the descriptive detail message explaining the specific file system or IO failure cause
     */
    public FileRegistryException(String message) {
        super(message);
    }
}
