package academy.io.output;

import academy.util.AnalysisContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonWriterTest {
    private final Writer writer = new JsonWriter();
    private static final AnalysisContext context = AnalysisContextFactory.createFromExampleData();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("Проверка соответствия формата JSON")
    void writeTest(@TempDir Path tempDir) throws Exception {
        Path outputFile = tempDir.resolve("test_output.json");

        writer.write(outputFile, context);

        assertTrue(Files.exists(outputFile), "Файл должен быть создан");

        String jsonContent = Files.readString(outputFile);
        assertNotNull(jsonContent, "Содержимое файла не должно быть null");
        assertFalse(jsonContent.trim().isEmpty(), "Файл не должен быть пустым");

        JsonNode rootNode = mapper.readTree(jsonContent);

        assertTrue(rootNode.has("files"), "JSON должен содержать поле 'files'");
        assertTrue(rootNode.has("totalRequestsCount"), "JSON должен содержать поле 'totalRequestsCount'");
        assertTrue(rootNode.has("responseSizeInBytes"), "JSON должен содержать поле 'responseSizeInBytes'");
        assertTrue(rootNode.has("resources"), "JSON должен содержать поле 'resources'");
        assertTrue(rootNode.has("responseCodes"), "JSON должен содержать поле 'responseCodes'");
        assertTrue(rootNode.has("requestsPerDate"), "JSON должен содержать поле 'requestsPerDate'");
        assertTrue(rootNode.has("uniqueProtocols"), "JSON должен содержать поле 'uniqueProtocols'");

        assertEquals(10000, rootNode.get("totalRequestsCount").asInt(),
            "totalRequestsCount должен быть 10000");

        JsonNode sizeNode = rootNode.get("responseSizeInBytes");
        assertTrue(sizeNode.has("average"), "responseSizeInBytes должен содержать 'average'");
        assertTrue(sizeNode.has("max"), "responseSizeInBytes должен содержать 'max'");
        assertTrue(sizeNode.has("p95"), "responseSizeInBytes должен содержать 'p95'");

        assertEquals(500.0, sizeNode.get("average").asDouble(), 0.001,
            "average должен быть 500.0");
        assertEquals(1000, sizeNode.get("max").asInt(),
            "max должен быть 1000");
        assertEquals(950, sizeNode.get("p95").asInt(),
            "p95 должен быть 950");

        JsonNode filesNode = rootNode.get("files");
        assertTrue(filesNode.isArray(), "files должен быть массивом");
        assertEquals(2, filesNode.size(), "files должен содержать 2 элемента");
        assertEquals("access.log", filesNode.get(0).asText());
        assertEquals("http://example.com/access.log", filesNode.get(1).asText());

        JsonNode resourcesNode = rootNode.get("resources");
        assertTrue(resourcesNode.isArray(), "resources должен быть массивом");
        assertEquals(2, resourcesNode.size(), "resources должен содержать 2 элемента");

        JsonNode firstResource = resourcesNode.get(0);
        assertEquals("/downloads/product_1", firstResource.get("resource").asText());
        assertEquals(1000, firstResource.get("totalRequestsCount").asInt());

        JsonNode protocolsNode = rootNode.get("uniqueProtocols");
        assertTrue(protocolsNode.isArray(), "uniqueProtocols должен быть массивом");
        assertEquals(3, protocolsNode.size(), "uniqueProtocols должен содержать 3 элемента");

        AnalysisContext parsedContext = mapper.readValue(jsonContent, AnalysisContext.class);
        assertNotNull(parsedContext, "JSON должен корректно парситься обратно в AnalysisContext");
        assertEquals(context.getTotalRequestsCount(), parsedContext.getTotalRequestsCount(),
            "totalRequestsCount должен совпадать после парсинга");
    }

    @Test
    @DisplayName("Проверка обработки ошибок при записи")
    void write_ShouldThrowException_WhenInvalidPath(@TempDir Path tempDir) {
        Path invalidPath = tempDir.resolve("123/test.json");

        assertThrows(RuntimeException.class,
            () -> writer.write(invalidPath, context),
            "Должно выбрасываться RuntimeException при ошибке записи");
    }

    @Test
    @DisplayName("Проверка формата дат в JSON")
    void dateFormatTest(@TempDir Path tempDir) throws Exception {
        Path outputFile = tempDir.resolve("date_test.json");

        writer.write(outputFile, context);

        String jsonContent = Files.readString(outputFile);
        JsonNode rootNode = mapper.readTree(jsonContent);
        JsonNode datesNode = rootNode.get("requestsPerDate");

        assertTrue(datesNode.isArray(), "requestsPerDate должен быть массивом");
        assertEquals(1, datesNode.size(), "Должна быть одна запись о дате");

        JsonNode dateNode = datesNode.get(0);
        assertEquals("2024-03-01", dateNode.get("date").asText());
        assertEquals("Monday", dateNode.get("weekday").asText());
        assertEquals(2981, dateNode.get("totalRequestsCount").asInt());
        assertEquals(12.10, dateNode.get("totalRequestsPercentage").asDouble(), 0.001);
    }
}
