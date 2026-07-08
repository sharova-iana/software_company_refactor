package org.informatics.data.enums;

import java.io.Serializable;

/**
 * <p>Represents the standardized professional role tiers and career positions within the software engineering company.</p>
 * <p>This enumeration serves as a critical structural configuration token. It controls downstream salary baseline
 * evaluations, management hierarchy boundaries, and financial reporting statistical aggregations. </p>
 */
public enum Position implements Serializable {
    /**
     * <p>Represents an entry-level software developer track.</p>
     * <p>Subject to baseline junior compensation scale limits.</p>
     */
    JUNIOR_DEVELOPER,

    /**
     * <p>Represents an advanced or principal engineering contributor track.</p>
     * <p>Subject to senior compensation boundaries and eligibility frameworks.</p>
     */
    SENIOR_DEVELOPER,

    /**
     * <p>Represents a quality assurance or test-automation contributor track.</p>
     */
    QA_ENGINEER,

    /**
     * <p>Represents a user-interface or user-experience creative contributor track.</p>
     */
    UI_UX_DESIGNER,

    /**
     * <p>Represents a leadership or team organization executive track.</p>
     * <p><strong>Strict Constraint:</strong> Only personnel assigned to this position are
     * mathematically eligible to serve as a manager or to lead an active team entity block.</p>
     */
    MANAGER;

}
