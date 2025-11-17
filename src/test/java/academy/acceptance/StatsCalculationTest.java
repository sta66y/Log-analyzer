package academy.acceptance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import academy.cli.LogAnalyzerCommand;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class StatsCalculationTest {

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
    @DisplayName("Расчет статистики на основании локального log-файла")
    void happyPathTest() throws IOException {
        Path logFile = tempDir.resolve("access.log");
        Files.write(logFile, List.of(
            "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /product1 HTTP/1.1\" 200 1000 \"-\" \"Debian\"",
            "80.91.33.133 - - [17/May/2015:08:05:34 +0000] \"GET /product1 HTTP/1.1\" 200 500 \"-\" \"Debian\"",
            "217.168.17.5 - - [17/May/2015:12:25:42 +0000] \"GET /product2 HTTP/1.1\" 404 0 \"-\" \"Debian\"",
            "46.105.14.53 - - [18/May/2015:14:30:15 +0000] \"GET /image.png HTTP/1.1\" 200 2500 \"-\" \"Mozilla\""
        ));

        Path outputFile = tempDir.resolve("stats.json");

        int exitCode = cmd.execute(
            "-p", logFile.toString(),
            "-f", "json",
            "-o", outputFile.toString()
        );

        assertEquals(0, exitCode);
        assertTrue(Files.exists(outputFile));

        String content = Files.readString(outputFile);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode result = mapper.readTree(content);

        assertEquals(4, result.get("totalRequestsCount").asInt());

        JsonNode responseSize = result.get("responseSizeInBytes");
        assertEquals(1000, responseSize.get("average").asDouble(), 0.01);
        assertEquals(2500, responseSize.get("max").asInt());

        JsonNode resources = result.get("resources");
        assertEquals(3, resources.size());

        JsonNode responseCodes = result.get("responseCodes");
        assertEquals(2, responseCodes.size());

        JsonNode requestsPerDate = result.get("requestsPerDate");
        assertEquals(2, requestsPerDate.size());
    }
}
