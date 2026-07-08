package org.informatics.exceptions;

import java.io.Serial;

/**
 * <p>Thrown when attempting to hire an employee to a position that does not have an entry-level floor set up.</p>
 * <p>This exception enforces corporate compliance by preventing the creation of new employment contracts
 * under a professional role track until the finance layer explicitly defines its initial minimum base salary bounds.</p>
 */
public class SalaryConfigurationException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1151898090801661964L;

    /**
     * <p>Constructs a new SalaryConfigurationException with the specified descriptive error message.</p>
     *
     * @param message the descriptive detail message explaining the missing baseline salary configuration cause
     */
    public SalaryConfigurationException(String message) {
        super(message);
    }
}
