package chad;

import java.util.Optional;

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
    private final Optional<String> loadWarning;

    /**
     * Returns a Chad object.
     * @param filePath file path for the data file
     */
    public Chad(String filePath) {
        this.tasks = new TaskList();
        this.storage = new Storage(filePath);
        this.loadWarning = storage.read(tasks);
    }

    /**
     * Gets a warning about tasks that could not be loaded from the save file.
     * @return the warning, or empty if everything loaded.
     */
    public Optional<String> getLoadWarning() {
        return loadWarning;
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
        } catch (RuntimeException e) {
            // A bug, not bad input: still reply instead of leaving the user with no response
            e.printStackTrace();
            return "Sorry, something went wrong while running that command (" + e + ").";
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
