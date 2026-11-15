package chad;

import chad.command.Command;
import chad.exception.ChadException;
import chad.storage.Storage;
import chad.tasklist.TaskList;

/**
 * Class that encapsulates the chatbot.
 */
public class Chad {
    private final TaskList tasks;
    private final Storage storage;

    /**
     * Returns a Chad object.
     * @param filePath file path for the data file
     */
    public Chad(String filePath) {
        this.tasks = new TaskList();
        this.storage = new Storage(filePath);
        storage.read(tasks);
    }

    /**
     * Returns response to user input.
     * @param cmd Command to execute.
     * @return chatbot response.
     */
    public String getResponse(Command cmd) {
        try {
            return cmd.execute(tasks, storage);
        } catch (ChadException e) {
            return e.getMessage();
        }
    }

    /**
     * Gets initial hello message.
     * @return hello message.
     */
    public static String getHelloMessage() {
        return "Hello! I'm Chad.\n"
                + "What can I do for you?\n"
                + "Type \"help\" to see a list of commands.";
    }
}
