package chad.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * A point in time for an event: a date with an optional time of day.
 * Written as YYYY-MM-DD or YYYY-MM-DD HH:mm.
 */
public class EventTime {
    private static final DateTimeFormatter DATE_INPUT = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DATE_TIME_INPUT = DateTimeFormatter.ofPattern("uuuu-MM-dd H:mm")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DATE_OUTPUT = DateTimeFormatter.ofPattern("MMM d yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final LocalDate date;
    private final LocalTime time; // null if only a date was given

    private EventTime(LocalDate date, LocalTime time) {
        this.date = date;
        this.time = time;
    }

    /**
     * Parses a date, or a date and time.
     * @param text YYYY-MM-DD or YYYY-MM-DD HH:mm (a single-digit hour is also accepted).
     * @return the parsed EventTime.
     * @throws DateTimeParseException if text is not in either format or is not a real date/time.
     */
    public static EventTime parse(String text) throws DateTimeParseException {
        String trimmed = text.strip();
        if (trimmed.contains(" ")) {
            LocalDateTime dateTime = LocalDateTime.parse(trimmed, DATE_TIME_INPUT);
            return new EventTime(dateTime.toLocalDate(), dateTime.toLocalTime());
        }
        return new EventTime(LocalDate.parse(trimmed, DATE_INPUT), null);
    }

    /**
     * Gets the earliest moment this could mean: the given time, or the start of the day.
     * @return the earliest moment.
     */
    public LocalDateTime earliest() {
        return date.atTime(time == null ? LocalTime.MIN : time);
    }

    /**
     * Gets the latest moment this could mean: the given time, or the end of the day.
     * @return the latest moment.
     */
    public LocalDateTime latest() {
        return date.atTime(time == null ? LocalTime.MAX : time);
    }

    /**
     * Formats this for display, e.g. "Dec 31 2025 14:00" or "Dec 31 2025".
     * @return the display string.
     */
    public String toDisplayString() {
        String day = date.format(DATE_OUTPUT);
        return time == null ? day : day + " " + time.format(TIME_FORMAT);
    }

    /**
     * Formats this in the input format, for saving, e.g. "2025-12-31 14:00" or "2025-12-31".
     * @return the string in input format.
     */
    @Override
    public String toString() {
        return time == null ? date.toString() : date + " " + time.format(TIME_FORMAT);
    }
}
