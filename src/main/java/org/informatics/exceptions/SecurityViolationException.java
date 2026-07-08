package org.informatics.exceptions;

import java.io.Serial;

/**
 * <p>Thrown when attempting to load or import data spreadsheets belonging to an incorrect or mismatched corporate workspace.</p>
 * <p>This exception enforces structural security boundaries within the application by preventing the cross-loading
 * and contamination of files between unauthorized company database instances.</p>
 */
public class SecurityViolationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -8749806234057224384L;

    /**
     * <p>Constructs a new SecurityViolationException with the specified descriptive error message.</p>
     *
     * @param message the descriptive detail message explaining the specific security cross-contamination violation cause
     */
    public SecurityViolationException(String message) {
        super(message);
    }
}
