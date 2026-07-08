package org.informatics.exceptions;

import java.io.Serial;

/**
 * <p>Thrown when a requested data entity cannot be found within the company registries.</p>
 * <p>This exception is typically triggered during system lookups when a specified unique identifier
 * (such as an employee ID, team ID, or contract number) does not map to an existing record in memory.</p>
 */
public class EntityNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 2498151890940650849L;

    /**
     * <p>Constructs a new EntityNotFoundException with the specified descriptive error message.</p>
     *
     * @param message the descriptive detail message explaining which entity could not be located
     */
    public EntityNotFoundException(String message) {
        super(message);
    }
}
