package chad.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import chad.exception.StorageException;
import chad.task.DeadlineTask;
import chad.task.EventTask;
import chad.task.TodoTask;
import chad.tasklist.TaskList;

public class StorageTest {
    @TempDir
    Path dir;

    private Path saveFile() {
        return dir.resolve("data").resolve("data.csv");
    }

    private TaskList load(Storage storage) {
        TaskList tasks = new TaskList();
        storage.read(tasks);
        return tasks;
    }

    private List<Path> backups() throws IOException {
        try (Stream<Path> files = Files.list(saveFile().getParent())) {
            return files.filter(p -> p.getFileName().toString().startsWith("data.csv.bak-")).toList();
        }
    }

    @Test
    public void roundTrip_commasAndQuotesInFields_preserved() {
        Storage storage = new Storage(saveFile().toString());
        TaskList tasks = new TaskList();
        tasks.add(new TodoTask("buy eggs, milk"));
        tasks.add(new DeadlineTask("essay, part \"2\"", "2025-09-22"));
        tasks.add(new EventTask("lunch, then \"talk\"", "2025-12-01 12:00", "2025-12-01"));
        storage.write(tasks.toCsvString());

        TaskList reloaded = load(new Storage(saveFile().toString()));
        assertEquals(tasks.toString(), reloaded.toString());
        assertEquals(tasks.toCsvString(), reloaded.toCsvString());
    }

    @Test
    public void bareFilename_noFolder_savesAndLoadsInCurrentFolder() throws IOException {
        // A bare filename has no parent folder, so it must be created in the current folder
        String name = "storage-test-" + System.nanoTime() + ".csv";
        Path inCurrentFolder = Path.of(name).toAbsolutePath();
        try {
            Storage storage = new Storage(name);
            assertTrue(Files.exists(inCurrentFolder));

            storage.write("T,0,bare\n");
            assertEquals("1.[T][ ] bare", load(new Storage(name)).toString());
        } finally {
            try (Stream<Path> leftovers = Files.list(inCurrentFolder.getParent())) {
                for (Path p : leftovers.filter(p -> p.getFileName().toString().startsWith(name)).toList()) {
                    Files.deleteIfExists(p);
                }
            }
        }
    }

    @Test
    public void read_oldFormatFile_loadsWithoutWarning() throws IOException {
        Files.createDirectories(saveFile().getParent());
        Files.writeString(saveFile(), "T,1,go home\n"
                + "D,0,finish ip,2025-09-22\n"
                + "E,0,meeting,2025-12-31 14:00,2025-12-31 15:00\n");

        Storage storage = new Storage(saveFile().toString());
        TaskList tasks = new TaskList();
        Optional<String> warning = storage.read(tasks);

        assertTrue(warning.isEmpty());
        assertEquals("1.[T][X] go home\n2.[D][ ] finish ip (by: Sep 22 2025)\n"
                + "3.[E][ ] meeting (from: Dec 31 2025 14:00 to: Dec 31 2025 15:00)",
                tasks.toString());
        assertTrue(backups().isEmpty());
    }

    @Test
    public void read_badLines_skippedWithWarningAndBackup() throws IOException {
        String original = "X,0,unknown type\n"
                + "T,0,good one\n"
                + "D,0,bad date,tomorrow\n"
                + "D,0,a, b,2025-01-01\n"
                + "E,0,missing end,2025-12-31\n"
                + "T,2,bad done flag\n"
                + "\n"
                + "T,1,good two\n";
        Files.createDirectories(saveFile().getParent());
        Files.writeString(saveFile(), original);

        Storage storage = new Storage(saveFile().toString());
        TaskList tasks = new TaskList();
        Optional<String> warning = storage.read(tasks);

        assertEquals("1.[T][ ] good one\n2.[T][X] good two", tasks.toString());
        assertTrue(warning.isPresent());
        assertTrue(warning.get().contains("5 lines"), warning.get());
        assertTrue(warning.get().contains("lines 1, 3, 4, 5, 6"), warning.get());

        List<Path> backups = backups();
        assertEquals(1, backups.size());
        assertEquals(original, Files.readString(backups.get(0)));
    }

    @Test
    public void write_failure_throwsAndKeepsOldFile() throws IOException {
        Storage storage = new Storage(saveFile().toString());
        storage.write("T,0,keep me\n");

        Path folder = saveFile().getParent();
        assumeFalse(!folder.toFile().setWritable(false) || Files.isWritable(folder),
                "cannot make folder read-only here (e.g. running as root)");
        try {
            assertThrows(StorageException.class, () -> storage.write("T,0,new\n"));
        } finally {
            folder.toFile().setWritable(true);
        }
        assertEquals("T,0,keep me\n", Files.readString(saveFile()));
    }
}
