package org.informatics.exceptions;

import java.io.Serial;

/**
 * <p>Thrown when an employee's age validation check fails during the hiring process.</p>
 * <p>This exception is triggered if the calculated age of an applicant based on their birthdate
 * falls outside the mandatory company policy limits (under 18 or over 70 years old).</p>
 */
public class AgeBoundaryException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 4272173558801274311L;

    /**
     * <p>Constructs a new AgeBoundaryException with the specified descriptive error message.</p>
     *
     * @param message the descriptive detail message explaining the specific boundary violation cause
     */
    public AgeBoundaryException(String message) {
        super(message);
    }
}
