package chad.exception;

/**
 * Indicates that the save file could not be written.
 */
public class StorageException extends ChadException {
    public StorageException(String message) {
        super(message);
    }
}
