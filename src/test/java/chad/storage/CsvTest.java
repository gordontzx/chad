package chad.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import chad.exception.CorruptFileException;

public class CsvTest {
    @Test
    public void join_plainFields_unquoted() {
        assertEquals("T,0,read book", Csv.join("T", "0", "read book"));
    }

    @Test
    public void join_fieldWithCommaOrQuote_quoted() {
        assertEquals("T,0,\"buy eggs, milk\"", Csv.join("T", "0", "buy eggs, milk"));
        assertEquals("T,0,\"say \"\"hi\"\"\"", Csv.join("T", "0", "say \"hi\""));
    }

    @Test
    public void split_oldUnquotedLine_readsAsBefore() {
        assertEquals(List.of("D", "0", "finish ip", "2025-09-22"), Csv.split("D,0,finish ip,2025-09-22"));
    }

    @Test
    public void split_emptyFields_kept() {
        assertEquals(List.of("a", "", ""), Csv.split("a,,"));
    }

    @Test
    public void roundTrip_trickyFields_unchanged() {
        String[] fields = {"E", "1", "a, \"b\", c", ",", "\"", "", "plain"};
        assertEquals(List.of(fields), Csv.split(Csv.join(fields)));
    }

    @Test
    public void split_unterminatedQuote_throws() {
        assertThrows(CorruptFileException.class, () -> Csv.split("T,0,\"oops"));
    }

    @Test
    public void split_textAfterClosingQuote_throws() {
        assertThrows(CorruptFileException.class, () -> Csv.split("T,0,\"a\"b"));
    }
}
