package org.informatics.exceptions;

import org.informatics.data.enums.Position;

import java.io.Serial;

/**
 * <p>Thrown within team formation pipelines if an employee selected to lead a new
 * team does not hold the required manager position tier.</p>
 * <p>This exception guarantees role compliance by preventing technical contributors
 * or developers from being designated as team managers.</p>
 */
public class PositionMismatchException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -1250388375344312764L;

    /**
     * <p>Constructs a new PositionMismatchException by formatting a detailed error message
     * from the employee's name, the required position, and their actual position.</p>
     *
     * @param employeeName the full name of the employee who failed the position validation check
     * @param expected     the mandatory {@link Position} required to execute the corporate action
     * @param actual       the actual {@link Position} currently held by the employee instance
     */
    public PositionMismatchException(String employeeName, Position expected, Position actual) {
        super(String.format("Position Mismatch: Employee '%s' cannot fulfill this action. Expected Position: [%s], but Employee is a [%s].",
                employeeName, expected, actual));
    }
}
