package chad.exception;

/**
 * Encapsulates most exceptions that can possibly happen during the runtime of the program.
 */
public class ChadException extends RuntimeException {
    public ChadException(String message) {
        super(message);
    }
}
