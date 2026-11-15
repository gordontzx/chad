package chad.storage;

import java.util.ArrayList;
import java.util.List;

import chad.exception.CorruptFileException;

/**
 * Encodes and decodes single lines of CSV.
 * A field is wrapped in double quotes only if it contains a comma or a double quote,
 * and any double quote inside it is doubled. Fields without these characters are
 * written as-is, so save files from older versions still read correctly.
 */
public class Csv {
    private static final char DELIMITER = ',';
    private static final char QUOTE = '"';

    private Csv() {
    }

    /**
     * Joins fields into one CSV line, quoting fields where needed.
     *
     * @param fields values to join.
     * @return the CSV line.
     */
    public static String join(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                sb.append(DELIMITER);
            }
            sb.append(quoteIfNeeded(fields[i]));
        }
        return sb.toString();
    }

    private static String quoteIfNeeded(String field) {
        boolean needsQuoting = field.indexOf(DELIMITER) != -1 || field.indexOf(QUOTE) != -1;
        if (!needsQuoting) {
            return field;
        }
        return QUOTE + field.replace("\"", "\"\"") + QUOTE;
    }

    /**
     * Splits one CSV line into its fields.
     *
     * @param line the CSV line.
     * @return the fields in order.
     * @throws CorruptFileException if a quoted field is malformed.
     */
    public static List<String> split(String line) throws CorruptFileException {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean isInQuotes = false;
        boolean wasQuoted = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (isInQuotes) {
                if (c != QUOTE) {
                    current.append(c);
                } else if (i + 1 < line.length() && line.charAt(i + 1) == QUOTE) {
                    current.append(QUOTE); // escaped quote
                    i++;
                } else {
                    isInQuotes = false; // closing quote
                }
            } else if (c == DELIMITER) {
                fields.add(current.toString());
                current.setLength(0);
                wasQuoted = false;
            } else if (c == QUOTE && current.length() == 0 && !wasQuoted) {
                isInQuotes = true; // opening quote
                wasQuoted = true;
            } else if (wasQuoted) {
                throw new CorruptFileException(); // text after a closing quote
            } else {
                current.append(c);
            }
        }

        if (isInQuotes) {
            throw new CorruptFileException(); // unterminated quote
        }
        fields.add(current.toString());
        return fields;
    }
}
