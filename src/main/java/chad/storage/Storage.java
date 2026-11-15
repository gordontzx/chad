package chad.storage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import chad.exception.CorruptFileException;
import chad.exception.StorageException;
import chad.task.DeadlineTask;
import chad.task.EventTask;
import chad.task.Task;
import chad.task.TodoTask;
import chad.tasklist.TaskList;

/*
Save file format (one task per line, see Csv for quoting rules):
<type>,<is_done>,<task_name>[,<by> | ,<from>,<to>]

types:
T: todo task
D: deadline task, <by> is a date in YYYY-MM-DD format
E: event task, <from> and <to> are YYYY-MM-DD or YYYY-MM-DD HH:mm

e.g.
T,1,read book
D,0,return book,2025-06-06
E,0,project meeting,2025-08-06 14:00,2025-08-06 16:00
T,0,"buy eggs, milk"
*/

/**
 * Object that handles file IO for chatbot.
 */
public class Storage {
    private static final Charset CHARSET = Charset.defaultCharset();
    private static final DateTimeFormatter BACKUP_SUFFIX_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final int MAX_LINE_NUMBERS_SHOWN = 5;

    private final File file;

    /**
     * Returns a Storage object that handles file IO.
     * @param filePath path of the save file.
     */
    public Storage(String filePath) {
        this.file = new File(filePath);

        // Create file if it does not exist. Use the absolute path so a bare filename
        // (no folder) still has a parent, namely the current folder.
        try {
            file.getAbsoluteFile().getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes content into file, replacing its previous contents.
     * The content is written to a temporary file first, so a failure part-way
     * through never leaves a half-written save file behind.
     *
     * @param contents to write into the file.
     * @throws StorageException if the file could not be written.
     */
    public void write(String contents) throws StorageException {
        Path target = file.toPath();
        Path temp = null;
        try {
            Path dir = target.toAbsolutePath().getParent();
            temp = Files.createTempFile(dir, file.getName(), ".tmp");
            Files.writeString(temp, contents, CHARSET);
            moveReplacing(temp, target);
        } catch (IOException e) {
            deleteQuietly(temp);
            throw new StorageException("Your change was made, but I couldn't save it to " + file.getPath()
                    + " (" + e.getMessage() + ").\nIt will be lost when you close Chad unless saving starts working.");
        }
    }

    private static void moveReplacing(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // Nothing more can be done; a stray temp file is harmless.
        }
    }

    /**
     * Reads tasks from the save file and adds them into the list of tasks.
     * Lines that cannot be read are skipped rather than stopping the load. If any are
     * skipped, the original file is backed up before it can be overwritten.
     *
     * @param tasks TaskList object.
     * @return a warning for the user if anything could not be loaded, otherwise empty.
     */
    public Optional<String> read(TaskList tasks) {
        if (!file.exists()) {
            return Optional.empty();
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath(), CHARSET);
        } catch (IOException e) {
            return Optional.of("I couldn't read your saved tasks from " + file.getPath()
                    + " (" + e.getMessage() + "), so I've started with an empty list.\n"
                    + backUpAndDescribe());
        }

        List<Integer> badLineNumbers = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            try {
                tasks.add(parseFileLine(line));
            } catch (RuntimeException e) {
                // Covers malformed fields, wrong field counts and invalid dates.
                badLineNumbers.add(i + 1);
            }
        }

        if (badLineNumbers.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(describeBadLines(badLineNumbers) + "\n" + backUpAndDescribe());
    }

    private String describeBadLines(List<Integer> badLineNumbers) {
        int count = badLineNumbers.size();
        String shown = badLineNumbers.stream()
                .limit(MAX_LINE_NUMBERS_SHOWN)
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
        String more = count > MAX_LINE_NUMBERS_SHOWN ? ", ..." : "";
        return String.format("Warning: %d line%s in %s couldn't be read and %s skipped (line%s %s%s).",
                count, count == 1 ? "" : "s", file.getPath(), count == 1 ? "was" : "were",
                count == 1 ? "" : "s", shown, more);
    }

    /**
     * Copies the save file to a timestamped backup next to it.
     * @return a message telling the user where the backup is, or that it failed.
     */
    private String backUpAndDescribe() {
        String suffix = LocalDateTime.now().format(BACKUP_SUFFIX_FORMAT);
        Path backup = file.toPath().resolveSibling(file.getName() + ".bak-" + suffix);
        try {
            Files.copy(file.toPath(), backup, StandardCopyOption.REPLACE_EXISTING);
            return "A copy of the original file was saved to " + backup + ".";
        } catch (IOException e) {
            return "I also couldn't back it up (" + e.getMessage() + "), so copy " + file.getPath()
                    + " somewhere safe before making changes.";
        }
    }

    private Task parseFileLine(String line) throws CorruptFileException {
        List<String> parts = Csv.split(line);
        if (parts.size() < 3) {
            throw new CorruptFileException();
        }

        String type = parts.get(0);
        boolean isDone = parseIsDone(parts.get(1));
        String description = parts.get(2);

        switch (type) {
        case "T":
            requireFieldCount(parts, 3);
            return new TodoTask(description, isDone);
        case "D":
            requireFieldCount(parts, 4);
            return new DeadlineTask(description, isDone, parts.get(3));
        case "E":
            requireFieldCount(parts, 5);
            return new EventTask(description, isDone, parts.get(3), parts.get(4));
        default:
            throw new CorruptFileException();
        }
    }

    private static boolean parseIsDone(String field) throws CorruptFileException {
        switch (field) {
        case "1":
            return true;
        case "0":
            return false;
        default:
            throw new CorruptFileException();
        }
    }

    private static void requireFieldCount(List<String> parts, int expected) throws CorruptFileException {
        if (parts.size() != expected) {
            throw new CorruptFileException();
        }
    }
}
