package chad.task;

import chad.storage.Csv;

/**
 * Encapsulates all types of tasks that the program can handle.
 */
public abstract class Task {
    private String description;
    private boolean isDone;

    public Task(String description) {
        this(description, false);
    }

    /**
     * Returns a Task object.
     * @param description task description.
     * @param isDone denotes whether the task is done.
     */
    public Task(String description, boolean isDone) {
        this.description = description;
        this.isDone = isDone;
    }

    public void markAsDone() {
        this.isDone = true;
    }

    public void unmarkAsDone() {
        this.isDone = false;
    }

    public boolean getIsDone() {
        return this.isDone;
    }

    public String getDescription() {
        return this.description;
    }

    /**
     * Returns a char indicating whether the task is completed.
     * @return 'X' if task is done, ' ' otherwise.
     */
    public char getStatusIcon() {
        return isDone ? 'X' : ' ';
    }

    /**
     * Fetches the task represented as a line for storage in csv format.
     * @return A string in csv format.
     */
    public abstract String toCsvString();

    /**
     * Builds the csv line for this task, quoting fields where needed.
     * @param typeCode single-letter code identifying the task type.
     * @param extraFields type-specific fields that follow the description.
     * @return A string in csv format.
     */
    protected String toCsvString(String typeCode, String... extraFields) {
        String[] fields = new String[3 + extraFields.length];
        fields[0] = typeCode;
        fields[1] = isDone ? "1" : "0";
        fields[2] = description;
        System.arraycopy(extraFields, 0, fields, 3, extraFields.length);
        return Csv.join(fields);
    }

    @Override
    public String toString() {
        return String.format("[%c] %s", getStatusIcon(), description);
    }
}
