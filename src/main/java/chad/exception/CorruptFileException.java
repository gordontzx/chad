package chad.exception;

/**
 * Exception indicating data being stored wrongly in save file.
 */
public class CorruptFileException extends ChadException {
    public CorruptFileException() {
        super("Corrupt file");
    }
}
