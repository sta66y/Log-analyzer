package academy.parser;

import academy.model.ParsedLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PathParserTest {

    private final PathParser pathParser = new PathParser();
    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z");

    @Test
    @DisplayName("Должен возвращать распаршенный лог, если нет ошибок ")
    void parseLine_ShouldReturnParsedLog_WhenRightLog() throws IOException {
        String line = "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"";

        ParsedLog parsedLog = pathParser.parseLine(line);

        ZonedDateTime date = ZonedDateTime.parse("17/May/2015:08:05:32 +0000", DATE_FORMAT);

        ParsedLog expected = new ParsedLog(
            "93.180.71.3", "-", "-", date,
            "GET", "/downloads/product_1", "HTTP/1.1", 304, 0, "-",
            "Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)"
        );

        assertEquals(expected, parsedLog);
    }
}
