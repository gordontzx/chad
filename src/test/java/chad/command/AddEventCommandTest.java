package chad.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import chad.exception.InvalidArgumentException;
import chad.storage.Storage;
import chad.tasklist.TaskList;

public class AddEventCommandTest {
    @TempDir
    Path dir;

    private TaskList tasks;
    private Storage storage;

    @BeforeEach
    public void setUp() {
        tasks = new TaskList();
        storage = new Storage(dir.resolve("data.csv").toString());
    }

    private void assertRejected(String args) {
        assertThrows(InvalidArgumentException.class, () -> new AddEventCommand(args).execute(tasks, storage),
                "should reject: " + args);
        assertEquals("List is empty!", tasks.toString());
    }

    @Test
    public void execute_validArgs_addsEvent() {
        new AddEventCommand("meeting /from 2pm /to 3pm").execute(tasks, storage);
        assertEquals("1.[E][ ] meeting (from: 2pm to: 3pm)", tasks.toString());
    }

    @Test
    public void execute_toBeforeFrom_rejectedNotCrash() {
        assertRejected("x /to 3pm /from 2pm");
    }

    @Test
    public void execute_missingOrBlankParts_rejected() {
        assertRejected("meeting /from 2pm");
        assertRejected("meeting /to 3pm");
        assertRejected("/from 2pm /to 3pm");
        assertRejected("   /from 2pm /to 3pm");
        assertRejected("meeting /from /to 3pm");
        assertRejected("meeting /from    /to 3pm");
        assertRejected("meeting /from 2pm /to   ");
        assertRejected("");
    }

    @Test
    public void execute_extraSpacesAroundParts_trimmed() {
        new AddEventCommand("meeting   /from   2pm   /to   3pm").execute(tasks, storage);
        assertEquals("1.[E][ ] meeting (from: 2pm to: 3pm)", tasks.toString());
    }
}
