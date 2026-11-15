package chad.command;

import chad.exception.InvalidArgumentException;
import chad.storage.Storage;
import chad.task.EventTask;
import chad.tasklist.TaskList;

/**
 * Command that adds event tasks.
 */
public class AddEventCommand extends AddCommand {
    private static final String FORMAT = "event TASK_NAME /from START /to END";

    private static final String FROM_FLAG = " /from ";
    private static final String TO_FLAG = " /to ";

    public AddEventCommand(String arg) {
        super(arg);
    }

    public static String getFormat() {
        return FORMAT;
    }

    @Override
    public String execute(TaskList tasks, Storage storage) {
        // Only look for /to after /from, so "/to ... /from ..." is rejected instead of crashing
        int fromIndex = args.indexOf(FROM_FLAG);
        int toIndex = fromIndex == -1 ? -1 : args.indexOf(TO_FLAG, fromIndex + FROM_FLAG.length());
        if (toIndex == -1) {
            throw new InvalidArgumentException("Error! Usage: " + FORMAT);
        }

        String taskName = args.substring(0, fromIndex).strip();
        String start = args.substring(fromIndex + FROM_FLAG.length(), toIndex).strip();
        String end = args.substring(toIndex + TO_FLAG.length()).strip();
        if (taskName.isEmpty() || start.isEmpty() || end.isEmpty()) {
            throw new InvalidArgumentException("Error! Usage: " + FORMAT);
        }

        // Create task
        String res = tasks.add(new EventTask(taskName, start, end));
        storage.write(tasks.toCsvString());
        return res;
    }
}
