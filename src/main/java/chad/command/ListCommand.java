package chad.command;

import chad.storage.Storage;
import chad.tasklist.TaskList;

/**
 * Command that lists stored tasks.
 */
public class ListCommand extends Command {
    private static final String FORMAT = "list";

    public ListCommand() {
        super("");
    }

    public static String getFormat() {
        return FORMAT;
    }

    @Override
    public String execute(TaskList tasks, Storage storage) {
        return "Here are the tasks in your list:\n" + tasks.toString();
    }

    @Override
    public boolean isExit() {
        return false;
    }
}
