package academy.io.output;

import static academy.io.output.AnalysisContextFactory.createEmptyContext;
import static academy.io.output.AnalysisContextFactory.createTestContext;
import static org.junit.jupiter.api.Assertions.*;

import academy.model.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JsonWriterTest {
    private final Writer writer = new JsonWriter();

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Должен создавать JSON файл с правильной структурой")
    void shouldCreateJsonFileWithCorrectStructure() throws Exception {
        AnalysisContext context = createTestContext();
        Path outputFile = tempDir.resolve("test_report.json");

        writer.write(outputFile, context);

        assertTrue(Files.exists(outputFile), "Файл должен быть создан");

        String content = Files.readString(outputFile);
        assertNotNull(content);
        assertFalse(content.trim().isEmpty());

        assertTrue(content.contains("\"files\""));
        assertTrue(content.contains("\"totalRequestsCount\""));
        assertTrue(content.contains("\"responseSizeInBytes\""));
        assertTrue(content.contains("\"resources\""));
        assertTrue(content.contains("\"responseCodes\""));
        assertTrue(content.contains("\"requestsPerDate\""));
        assertTrue(content.contains("\"uniqueProtocols\""));
    }

    @Test
    @DisplayName("Должен правильно отображать общую информацию в JSON")
    void shouldDisplayGeneralInformationInJson() throws Exception {
        AnalysisContext context = createTestContext();
        Path outputFile = tempDir.resolve("general_info.json");

        writer.write(outputFile, context);

        String content = Files.readString(outputFile);

        assertTrue(content.contains("\"files\""), "Должно содержать поле files");
        assertTrue(content.contains("\"totalRequestsCount\""), "Должно содержать поле totalRequestsCount");
        assertTrue(content.contains("\"responseSizeInBytes\""), "Должно содержать поле responseSizeInBytes");

        assertTrue(content.contains("10000"), "Должно содержать общее количество запросов");
        assertTrue(content.contains("1000"), "Должно содержать максимальный размер ответа");
        assertTrue(content.contains("500.0"), "Должно содержать средний размер ответа");
        assertTrue(content.contains("950"), "Должно содержать 95 перцентиль");
    }

    @Test
    @DisplayName("Должен правильно форматировать список файлов в JSON")
    void shouldFormatFilesListInJson() throws Exception {
        AnalysisContext context = createTestContext();
        Path outputFile = tempDir.resolve("files_format.json");

        writer.write(outputFile, context);

        String content = Files.readString(outputFile);

        assertTrue(content.contains("access.log"));
        assertTrue(content.contains("http://example.com/access.log"));
    }

    @Test
    @DisplayName("Должен отображать ресурсы с правильными данными в JSON")
    void shouldDisplayResourcesWithCorrectDataInJson() throws Exception {
        AnalysisContext context = createTestContext();
        Path outputFile = tempDir.resolve("resources.json");

        writer.write(outputFile, context);

        String content = Files.readString(outputFile);

        assertTrue(content.contains("\"resources\""));

        assertTrue(content.contains("/downloads/product_1"));
        assertTrue(content.contains("/downloads/product_2"));
        assertTrue(content.contains("1000"));
        assertTrue(content.contains("100"));
    }

    @Test
    @DisplayName("Должен отображать HTTP коды с правильными именами в JSON")
    void shouldDisplayHttpCodesWithCorrectNamesInJson() throws Exception {
        AnalysisContext context = createTestContext();
        Path outputFile = tempDir.resolve("http_codes.json");

        writer.write(outputFile, context);

        String content = Files.readString(outputFile);

        assertTrue(content.contains("\"responseCodes\""));

        assertTrue(content.contains("200"));
        assertTrue(content.contains("401"));
        assertTrue(content.contains("500"));
        assertTrue(content.contains("1000"));
        assertTrue(content.contains("10"));
        assertTrue(content.contains("1"));
    }

    @Test
    @DisplayName("Должен отображать статистику по датам в правильном формате в JSON")
    void shouldDisplayDateStatisticsInCorrectFormatInJson() throws Exception {
        AnalysisContext context = createTestContext();
        Path outputFile = tempDir.resolve("dates.json");

        writer.write(outputFile, context);

        String content = Files.readString(outputFile);

        assertTrue(content.contains("\"requestsPerDate\""));

        assertTrue(content.contains("2024-03-01"));
        assertTrue(content.contains("Monday"));
        assertTrue(content.contains("2981"));
        assertTrue(content.contains("12.1"));
    }

    @Test
    @DisplayName("Должен отображать протоколы в JSON")
    void shouldDisplayProtocolsInJson() throws Exception {
        AnalysisContext context = createTestContext();
        Path outputFile = tempDir.resolve("protocols.json");

        writer.write(outputFile, context);

        String content = Files.readString(outputFile);

        assertTrue(content.contains("\"uniqueProtocols\""));

        assertTrue(content.contains("HTTP/1.1"));
        assertTrue(content.contains("HTTP/2.0"));
        assertTrue(content.contains("grpc"));
    }

    @Test
    @DisplayName("Должен обрабатывать контекст с пустыми данными")
    void shouldHandleContextWithEmptyData() throws Exception {
        AnalysisContext emptyContext = createEmptyContext();
        Path outputFile = tempDir.resolve("empty.json");

        writer.write(outputFile, emptyContext);

        assertTrue(Files.exists(outputFile));

        String content = Files.readString(outputFile);
        assertTrue(content.contains("\"files\""));
        assertTrue(content.contains("\"totalRequestsCount\""));
    }

    @Test
    @DisplayName("Должен выбрасывать исключение при ошибке записи файла")
    void shouldThrowExceptionOnWriteError() {
        Path invalidPath = tempDir.resolve("asdfasdf/asd/fsdr.json");

        AnalysisContext context = createTestContext();

        IOException exception = assertThrows(IOException.class, () -> writer.write(invalidPath, context));

        assertTrue(exception.getMessage().contains("Не удалось записать json в файл"));
    }
}
