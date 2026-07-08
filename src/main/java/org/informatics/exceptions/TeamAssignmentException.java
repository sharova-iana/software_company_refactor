package org.informatics.exceptions;

import java.io.Serial;

/**
 * <p>Thrown when a team operation violates corporate relational assignment and structural boundary rules.</p>
 * <p>This exception enforces strict company policies, including: preventing a manager from leading
 * multiple teams simultaneously, blocking regular contributor members from joining more than a single
 * team pool, and rejecting any attempt to add an employee holding a manager position as a regular member contributor.</p>
 */
public class TeamAssignmentException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -1432794085223589613L;

    /**
     * <p>Constructs a new TeamAssignmentException with the specified descriptive error message.</p>
     *
     * @param message the descriptive detail message explaining the specific team structural boundary violation cause
     */
    public TeamAssignmentException(String message) {
        super(message);
    }
}
