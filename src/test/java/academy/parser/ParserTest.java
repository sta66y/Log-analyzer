package academy.parser;

import org.junit.jupiter.api.Test;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParserTest {

    private final Parser parser = new Parser();
    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z");

    @Test
    void parse_ShouldReturnStream_WhenRightLog() {
        Stream<String> stream = Stream.of(
            "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"",
            "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 228 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"",
            "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 337 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\""
        );

        List<ParsedLog> parsedLogList = parser.parse(stream).toList();

        assertEquals(3, parsedLogList.size());

        ZonedDateTime date = ZonedDateTime.parse("17/May/2015:08:05:32 +0000", DATE_FORMAT);

        ParsedLog expected1 = new ParsedLog(
            "93.180.71.3", "-", "-", date,
            "GET /downloads/product_1 HTTP/1.1", 304, 0, "-",
            "Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)"
        );
        ParsedLog expected2 = new ParsedLog(
            "93.180.71.3", "-", "-", date,
            "GET /downloads/product_1 HTTP/1.1", 228, 0, "-",
            "Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)"
        );
        ParsedLog expected3 = new ParsedLog(
            "93.180.71.3", "-", "-", date,
            "GET /downloads/product_1 HTTP/1.1", 337, 0, "-",
            "Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)"
        );

        assertEquals(expected1, parsedLogList.get(0));
        assertEquals(expected2, parsedLogList.get(1));
        assertEquals(expected3, parsedLogList.get(2));
    }

    @Test
    void parse_ShouldThrowException_WhenLogIsWrong() {
        Stream<String> stream = Stream.of("biba");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> parser.parse(stream).toList()); // используем терминальную операцию, чтобы ошибка появилась

        assertTrue(ex.getMessage().contains("Ошибка парсинга"));
    }
}
