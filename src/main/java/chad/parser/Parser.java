package chad.parser;


import chad.command.AddDeadlineCommand;
import chad.command.AddEventCommand;
import chad.command.AddTodoCommand;
import chad.command.Command;
import chad.command.DeleteCommand;
import chad.command.ExitCommand;
import chad.command.FindCommand;
import chad.command.HelpCommand;
import chad.command.InvalidCommand;
import chad.command.ListCommand;
import chad.command.MarkCommand;
import chad.command.UnmarkCommand;

/**
 * Contains method(s) that parse commands sent by the user.
 */
public class Parser {
    /**
     * Parses user command.
     *
     * @param input Full command sent by user.
     * @return Command object that represents the command to be executed.
     */
    public static Command parse(String input) {
        String[] parts = splitInput(input);
        String cmd = parts[0];
        String args = parts[1];

        switch (cmd) {
        case "bye":
            return new ExitCommand();
        case "list":
            return new ListCommand();
        case "todo":
            return new AddTodoCommand(args);
        case "deadline":
            return new AddDeadlineCommand(args);
        case "event":
            return new AddEventCommand(args);
        case "mark":
            return new MarkCommand(args);
        case "unmark":
            return new UnmarkCommand(args);
        case "delete":
            return new DeleteCommand(args);
        case "find":
            return new FindCommand(args);
        case "help":
            return new HelpCommand(args);
        default:
            return new InvalidCommand();
        }
    }

    /**
     * Splits input into cmd and arg parts.
     * @param input user input.
     * @return array containing the parts.
     */
    private static String[] splitInput(String input) {
        // Ignore surrounding whitespace and any run of whitespace between the command and its arguments
        String[] parts = input.strip().split("\\s+", 2);
        String cmd = parts[0];
        String args = parts.length > 1 ? parts[1] : "";

        assert !cmd.contains(" ") : "Command keyword contains no spaces.";
        assert cmd.length() + args.length() <= input.length() : "Length of cmd and args do not exceed length of input.";

        return new String[]{cmd, args};
    }
}
