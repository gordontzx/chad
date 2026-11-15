package chad.exception;

/**
 * Indicates commands being supplied with invalid arguments.
 */
public class InvalidArgumentException extends ChadException {
    public InvalidArgumentException(String message) {
        super(message);
    }
}
