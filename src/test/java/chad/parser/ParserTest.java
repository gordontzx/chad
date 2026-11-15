package chad.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import chad.command.Command;
import chad.command.ExitCommand;
import chad.command.InvalidCommand;
import chad.command.ListCommand;
import chad.storage.Storage;
import chad.tasklist.TaskList;

public class ParserTest {
    @Test
    public void parseListCommandTest() {
        assertInstanceOf(ListCommand.class, Parser.parse("list"));
    }

    @Test
    public void parseExitCommandTest() {
        assertInstanceOf(ExitCommand.class, Parser.parse("bye"));
    }

    @Test
    public void parseReturnsCommandTest() {
        assertInstanceOf(Command.class, Parser.parse("todo event"));
    }

    @Test
    public void parseInvalidCommandTest() {
        assertInstanceOf(InvalidCommand.class, Parser.parse("aksldjf"));
    }

    @Test
    public void parse_surroundingWhitespace_ignored() {
        assertInstanceOf(ListCommand.class, Parser.parse(" list"));
        assertInstanceOf(ListCommand.class, Parser.parse("list  "));
        assertInstanceOf(ExitCommand.class, Parser.parse("\tbye\t"));
    }

    @Test
    public void parse_emptyOrBlankInput_invalid() {
        assertInstanceOf(InvalidCommand.class, Parser.parse(""));
        assertInstanceOf(InvalidCommand.class, Parser.parse("   "));
    }

    @Test
    public void parse_extraSpacesAroundArgs_trimmed(@TempDir Path dir) {
        TaskList tasks = new TaskList();
        Storage storage = new Storage(dir.resolve("data.csv").toString());
        Parser.parse("  todo   read book  ").execute(tasks, storage);
        Parser.parse("mark   1 ").execute(tasks, storage);
        assertEquals("1.[T][X] read book", tasks.toString());
    }
}
