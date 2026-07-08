package org.informatics.exceptions;

import java.io.Serial;

/**
 * <p>Thrown when a salary configuration violates the financial rules of the company.</p>
 * <p>This exception is triggered when trying to set a negative salary floor for a professional position,
 * or when a proposed employee salary falls below the minimum baseline floor configured for that role.</p>
 */
public class InvalidSalaryException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -444402591981515960L;

    /**
     * <p>Constructs a new InvalidSalaryException with the specified descriptive error message.</p>
     *
     * @param message the descriptive detail message explaining the specific financial boundary violation cause
     */
    public InvalidSalaryException(String message) {
        super(message);
    }
}
