package wbs.magic.exceptions;

import wbs.magic.controls.conditions.CastCondition;

/**
 * Thrown when a {@link CastCondition} doesn't support the provided event type.
 */
public class EventNotSupportedException extends Exception {
    public EventNotSupportedException() {}

    public EventNotSupportedException(String message) {
        super(message);
    }
}
