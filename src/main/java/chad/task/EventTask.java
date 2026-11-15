package chad.task;

import java.time.format.DateTimeParseException;

import chad.exception.InvalidArgumentException;

/**
 * Represents event tasks.
 */
public class EventTask extends Task {
    private final EventTime start;
    private final EventTime end;

    public EventTask(String description, String start, String end) {
        this(description, false, start, end);
    }

    /**
     * Returns a EventTask object.
     * @param description task description.
     * @param isDone denotes whether the task is done.
     * @param start start of the event, as YYYY-MM-DD or YYYY-MM-DD HH:mm.
     * @param end end of the event, as YYYY-MM-DD or YYYY-MM-DD HH:mm.
     * @throws DateTimeParseException if start or end is not a valid date/time.
     * @throws InvalidArgumentException if end is before start.
     */
    public EventTask(String description, boolean isDone, String start, String end)
            throws DateTimeParseException, InvalidArgumentException {
        super(description, isDone);
        this.start = EventTime.parse(start);
        this.end = EventTime.parse(end);
        // A date without a time covers the whole day, so compare the widest reading of each
        if (this.end.latest().isBefore(this.start.earliest())) {
            throw new InvalidArgumentException("The event can't end before it starts!");
        }
    }

    @Override
    public String toCsvString() {
        return toCsvString("E", start.toString(), end.toString());
    }

    @Override
    public String toString() {
        return String.format("[E]%s (from: %s to: %s)",
                super.toString(), start.toDisplayString(), end.toDisplayString());
    }
}
