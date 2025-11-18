package academy.acceptance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import academy.cli.LogAnalyzerCommand;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

public class StatsReportTest {

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
    @DisplayName("Сохранение статистики в формате JSON")
    void jsonTest() throws IOException {
        Path logFile = tempDir.resolve("access.log");
        Files.write(
                logFile,
                List.of(
                        "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /product1 HTTP/1.1\" 200 1000 \"-\" \"Debian\""));

        Path outputFile = tempDir.resolve("result.json");

        int exitCode = cmd.execute(
                "-p", logFile.toString(),
                "-f", "json",
                "-o", outputFile.toString());

        assertEquals(0, exitCode);
        assertTrue(Files.exists(outputFile));

        String content = Files.readString(outputFile);
        assertTrue(content.contains("\"totalRequestsCount\""));
        assertTrue(content.contains("\"resources\""));
    }

    @Test
    @DisplayName("Сохранение статистики в формате MARKDOWN")
    void markdownTest() throws IOException {
        Path logFile = tempDir.resolve("access.log");
        Files.write(
                logFile,
                List.of(
                        "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /product1 HTTP/1.1\" 200 1000 \"-\" \"Debian\""));

        Path outputFile = tempDir.resolve("result.md");

        int exitCode = cmd.execute(
                "-p", logFile.toString(),
                "-f", "markdown",
                "-o", outputFile.toString());

        assertEquals(0, exitCode);
        assertTrue(Files.exists(outputFile));

        String content = Files.readString(outputFile);
        assertTrue(content.contains("#") || content.contains("|"));
    }

    @Test
    @DisplayName("Сохранение статистики в формате ADOC")
    void adocTest() throws IOException {
        Path logFile = tempDir.resolve("access.log");
        Files.write(
                logFile,
                List.of(
                        "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /product1 HTTP/1.1\" 200 1000 \"-\" \"Debian\""));

        Path outputFile = tempDir.resolve("result.adoc");

        int exitCode = cmd.execute(
                "-p", logFile.toString(),
                "-f", "adoc",
                "-o", outputFile.toString());

        assertEquals(0, exitCode);
        assertTrue(Files.exists(outputFile));

        String content = Files.readString(outputFile);
        assertTrue(content.contains("=") || content.contains("|==="));
    }
}
