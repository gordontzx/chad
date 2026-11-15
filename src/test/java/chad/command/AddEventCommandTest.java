package chad.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import chad.exception.InvalidArgumentException;
import chad.storage.Storage;
import chad.tasklist.TaskList;

public class AddEventCommandTest {
    private static final String USAGE = "event TASK_NAME /from YYYY-MM-DD [HH:mm] /to YYYY-MM-DD [HH:mm]";

    @TempDir
    Path dir;

    private TaskList tasks;
    private Storage storage;

    @BeforeEach
    public void setUp() {
        tasks = new TaskList();
        storage = new Storage(dir.resolve("data.csv").toString());
    }

    private String add(String args) {
        new AddEventCommand(args).execute(tasks, storage);
        return tasks.toString();
    }

    private String assertRejected(String args) {
        AddEventCommand command = new AddEventCommand(args);
        InvalidArgumentException e = assertThrows(InvalidArgumentException.class, () -> command.execute(tasks, storage),
                "should reject: " + args);
        assertEquals("List is empty!", tasks.toString());
        return e.getMessage();
    }

    @Test
    public void execute_datesAndTimes_addsEvent() {
        assertEquals("1.[E][ ] meeting (from: Dec 31 2025 14:00 to: Dec 31 2025 15:00)",
                add("meeting /from 2025-12-31 14:00 /to 2025-12-31 15:00"));
    }

    @Test
    public void execute_datesOnly_addsEvent() {
        assertEquals("1.[E][ ] camp (from: Dec 1 2025 to: Dec 3 2025)",
                add("camp /from 2025-12-01 /to 2025-12-03"));
    }

    @Test
    public void execute_singleDigitHour_accepted() {
        assertEquals("1.[E][ ] standup (from: Dec 1 2025 09:00 to: Dec 1 2025 09:15)",
                add("standup /from 2025-12-01 9:00 /to 2025-12-01 9:15"));
    }

    @Test
    public void execute_sameDayDateOnlyEnd_coversWholeDay() {
        // An end with no time means "by the end of that day", so it is not before 14:00
        assertEquals("1.[E][ ] talk (from: Dec 1 2025 14:00 to: Dec 1 2025)",
                add("talk /from 2025-12-01 14:00 /to 2025-12-01"));
    }

    @Test
    public void execute_endBeforeStart_rejected() {
        String expected = "The event can't end before it starts!";
        assertEquals(expected, assertRejected("x /from 2025-12-02 /to 2025-12-01"));
        assertEquals(expected, assertRejected("x /from 2025-12-01 15:00 /to 2025-12-01 14:00"));
    }

    @Test
    public void execute_invalidDates_rejected() {
        String expected = "Invalid date! Usage: " + USAGE;
        assertEquals(expected, assertRejected("x /from banana /to yesterday"));
        assertEquals(expected, assertRejected("x /from 2pm /to 3pm"));
        assertEquals(expected, assertRejected("x /from 2025-02-30 /to 2025-03-01")); // no Feb 30
        assertEquals(expected, assertRejected("x /from 2025-12-01 25:00 /to 2025-12-02"));
        assertEquals(expected, assertRejected("x /from 31-12-2025 /to 2025-12-31"));
        assertEquals(expected, assertRejected("x /from 2025-12-01 2pm /to 2025-12-02"));
    }

    @Test
    public void execute_toBeforeFrom_rejectedNotCrash() {
        assertEquals("Error! Usage: " + USAGE, assertRejected("x /to 2025-12-02 /from 2025-12-01"));
    }

    @Test
    public void execute_missingOrBlankParts_rejected() {
        assertRejected("meeting /from 2025-12-01");
        assertRejected("meeting /to 2025-12-01");
        assertRejected("/from 2025-12-01 /to 2025-12-02");
        assertRejected("   /from 2025-12-01 /to 2025-12-02");
        assertRejected("meeting /from /to 2025-12-02");
        assertRejected("meeting /from    /to 2025-12-02");
        assertRejected("meeting /from 2025-12-01 /to   ");
        assertRejected("");
    }

    @Test
    public void execute_extraSpacesAroundParts_trimmed() {
        assertTrue(add("meeting   /from   2025-12-01   /to   2025-12-02  ")
                .endsWith("meeting (from: Dec 1 2025 to: Dec 2 2025)"));
    }
}
