package wbs.magic.exceptions;

/**
 * Thrown when a spell doesn't implement any castable
 */
public class UncastableSpellException extends RuntimeException {
    public UncastableSpellException() {}
    public UncastableSpellException(String message) {
        super(message);
    }
}
