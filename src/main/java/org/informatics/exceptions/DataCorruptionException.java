package org.informatics.exceptions;

import java.io.Serial;

/**
 * <p>Thrown when an imported data file contains structural formatting errors or missing elements.</p>
 * <p>This exception is typically triggered during CSV data parsing workflows if a row fails to
 * match the required layout schema, lacks mandatory fields, or contains invalid tokens.</p>
 */
public class DataCorruptionException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -3717305898507447548L;

    /**
     * <p>Constructs a new DataCorruptionException with the specified descriptive error message.</p>
     *
     * @param message the descriptive detail message explaining the specific file corruption cause
     */
    public DataCorruptionException(String message) {
        super(message);
    }
}
