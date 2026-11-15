package chad.command;

import chad.exception.InvalidArgumentException;
import chad.storage.Storage;
import chad.tasklist.TaskList;

/**
 * Command that marks a task as not done.
 */
public class UnmarkCommand extends Command {
    private static final String FORMAT = "unmark TASK_INDEX";

    public UnmarkCommand(String args) {
        super(args);
    }

    public static String getFormat() {
        return FORMAT;
    }

    @Override
    public String execute(TaskList tasks, Storage storage) throws InvalidArgumentException {
        try {
            String res = tasks.unmarkAsDone(Integer.parseInt(args));
            storage.write(tasks.toCsvString());
            return res;
        } catch (NumberFormatException e) {
            throw new InvalidArgumentException("Error! Usage: " + FORMAT);
        }
    }

    @Override
    public boolean isExit() {
        return false;
    }
}
