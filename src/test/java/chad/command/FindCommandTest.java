package chad.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import chad.exception.InvalidArgumentException;
import chad.task.TodoTask;
import chad.tasklist.TaskList;

public class FindCommandTest {
    private TaskList tasks;

    @BeforeEach
    public void setUp() {
        tasks = new TaskList();
        tasks.add(new TodoTask("borrow Book"));
        tasks.add(new TodoTask("go home"));
        tasks.add(new TodoTask("return book"));
    }

    private String find(String keyword) {
        return new FindCommand(keyword).execute(tasks, null); // find never touches storage
    }

    @Test
    public void execute_differentCase_matches() {
        assertEquals("Here are the matching tasks in your list:\n1.[T][ ] borrow Book\n2.[T][ ] return book",
                find("BOOK"));
    }

    @Test
    public void execute_noMatches_saysSo() {
        assertEquals("No tasks in your list match \"eggs\".", find("eggs"));
    }

    @Test
    public void execute_emptyKeyword_showsUsage() {
        InvalidArgumentException e = assertThrows(InvalidArgumentException.class, () -> find(""));
        assertEquals("Error! Usage: find KEYWORD", e.getMessage());
    }
}
