package academy.acceptance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import academy.cli.LogAnalyzerCommand;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LogFileParsingTest {

    private LogAnalyzerCommand logAnalyzerCommand;
    private CommandLine cmd;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setup() {
        logAnalyzerCommand = new LogAnalyzerCommand();
        cmd = new CommandLine(logAnalyzerCommand);
    }

    @Test
    @DisplayName("На вход передан валидный локальный log-файл")
    void localFileProcessingTest() throws IOException {
        Path logFile = tempDir.resolve("access.log");
        Files.write(logFile, "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"".getBytes());

        Path outputFile = tempDir.resolve("result.json");

        int exitCode = cmd.execute(
            "-p", logFile.toString(),
            "-f", "json",
            "-o", outputFile.toString()
        );

        assertEquals(0, exitCode);
        assertTrue(Files.exists(outputFile));
        assertTrue(Files.size(outputFile) > 0);
    }

    @Test
    @DisplayName("На вход передан валидный удаленный log-файл")
    void remoteFileProcessingTest() throws IOException {
        try (MockWebServer server = new MockWebServer()) {
            String logContent = "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"";

            server.enqueue(new MockResponse()
                .setBody(logContent)
                .setResponseCode(200)
                .addHeader("Content-Type", "text/plain"));

            server.start();

            Path outputFile = tempDir.resolve("result.json");
            String url = server.url("/logs/access.log").toString();

            int exitCode = cmd.execute(
                "-p", url,
                "-f", "json",
                "-o", outputFile.toString()
            );

            assertEquals(0, exitCode);
            assertTrue(Files.exists(outputFile));

            server.shutdown();
        }
    }

    @Test
    @DisplayName("На вход передан валидный локальный log-файл, часть строк в котором нужно отфильтровать по --from и --to")
    void localFileProcessingAndFilteringTest() throws IOException {
        Path logFile = tempDir.resolve("access.log");
        Files.write(logFile, List.of(
            "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"",  // Должен попасть в результат (17/May/2015)
            "80.91.33.133 - - [17/May/2015:08:05:34 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.17)\"",  // Должен попасть в результат (17/May/2015)
            "217.168.17.5 - - [18/May/2015:12:25:42 +0000] \"GET /downloads/product_2 HTTP/1.1\" 200 490 \"-\" \"Debian APT-HTTP/1.3 (0.8.10.3)\"",  // Должен быть отфильтрован (18/May/2015)
            "46.105.14.53 - - [16/May/2015:23:59:59 +0000] \"GET /downloads/product_1 HTTP/1.1\" 200 490 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\""  // Должен быть отфильтрован (16/May/2015)
        ));

        Path outputFile = tempDir.resolve("result.json");

        int exitCode = cmd.execute(
            "-p", logFile.toString(),
            "-f", "json",
            "-o", outputFile.toString(),
            "--from", "2015-05-17",
            "--to", "2015-05-17"
        );

        assertEquals(0, exitCode);
        assertTrue(Files.exists(outputFile));

        String content = Files.readString(outputFile);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode result = mapper.readTree(content);

        assertEquals(2, result.get("totalRequestsCount").asInt());
        assertEquals(0.0, result.get("responseSizeInBytes").get("average").asDouble(), 0.01);
        assertEquals(0.0, result.get("responseSizeInBytes").get("max").asInt());

        assertEquals(1, result.get("resources").size());
        assertEquals("/downloads/product_1", result.get("resources").get(0).get("resource").asText());

        assertEquals(1, result.get("requestsPerDate").size());
        assertEquals("2015-05-17", result.get("requestsPerDate").get(0).get("date").asText());
        assertEquals(2, result.get("requestsPerDate").get(0).get("totalRequestsCount").asInt());
    }

    @Test
    @DisplayName("На вход передан локальный log-файл, часть строк в котором не подходит под формат")
    void damagedLocalFileProcessingTest() throws IOException {
        Path logFile = tempDir.resolve("access.log");
        Files.write(logFile, List.of(
            "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.21)\"",  // Валидная строка
            "INVALID LOG ENTRY WITHOUT PROPER FORMAT",  // Невалидная строка
            "80.91.33.133 - - [17/May/2015:08:05:34 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian APT-HTTP/1.3 (0.8.16~exp12ubuntu10.17)\"",  // Валидная строка
            "",  // Пустая строка
            "217.168.17.5 - - [18/May/2015:12:25:42 +0000] \"GET /downloads/product_2 HTTP/1.1\" 200 490",  // Неполная строка (без user-agent)
            "another invalid [log] entry with wrong date format"  // Еще невалидная строка
        ));

        Path outputFile = tempDir.resolve("result.json");

        int exitCode = cmd.execute(
            "-p", logFile.toString(),
            "-f", "json",
            "-o", outputFile.toString()
        );

        assertEquals(2, exitCode);
    }
}
