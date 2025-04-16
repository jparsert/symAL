package utilities.exceptions;

public class InvariantViolationException extends RuntimeException {
    public InvariantViolationException(String message) {
        super(message);
    }
}
