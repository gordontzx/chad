package chad.parser;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;

import chad.command.Command;
import chad.command.ExitCommand;
import chad.command.InvalidCommand;
import chad.command.ListCommand;

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
}
