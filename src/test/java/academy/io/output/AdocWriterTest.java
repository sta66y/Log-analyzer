package academy.io.output;

import academy.util.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AdocWriterTest {
    private final Writer writer = new AdocWriter();

    @Test
    @DisplayName("Должен создавать AsciiDoc файл с правильной структурой")
    void shouldCreateAsciiDocFileWithCorrectStructure(@TempDir Path tempDir) throws Exception {
        AnalysisContext context = createTestContext();
        Path outputFile = tempDir.resolve("test_report.adoc");

        writer.write(outputFile, context);

        assertTrue(Files.exists(outputFile), "Файл должен быть создан");

        String content = Files.readString(outputFile);
        assertNotNull(content);
        assertFalse(content.trim().isEmpty());

        assertTrue(content.contains("= Анализ логов NGINX"));
        assertTrue(content.contains(":toc:"));
        assertTrue(content.contains(":toclevels: 3"));
        assertTrue(content.contains("==== Общая информация"));
        assertTrue(content.contains("==== Запрашиваемые ресурсы"));
        assertTrue(content.contains("==== Коды ответа"));
        assertTrue(content.contains("==== Статистика по датам"));
        assertTrue(content.contains("==== Статистика по протоколам"));
    }

    @Test
    @DisplayName("Должен правильно отображать общую информацию в таблице")
    void shouldDisplayGeneralInformationInTable(@TempDir Path tempDir) throws Exception {
        AnalysisContext context = createTestContext();
        Path outputFile = tempDir.resolve("general_info.adoc");

        writer.write(outputFile, context);

        String content = Files.readString(outputFile);

        assertTrue(content.contains("[cols=\"1,1\", options=\"header\"]"));
        assertTrue(content.contains("| Метрика | Значение"));
        assertTrue(content.contains("| Файл(-ы) |"));
        assertTrue(content.contains("| Начальная дата |"));
        assertTrue(content.contains("| Конечная дата |"));
        assertTrue(content.contains("| Количество запросов |"));
        assertTrue(content.contains("| Максимальный размер ответа |"));
        assertTrue(content.contains("| Средний размер ответа |"));
        assertTrue(content.contains("| 95p размера ответа |"));

        assertTrue(content.contains("10000"), "Должно содержать общее количество запросов");
        assertTrue(content.contains("1000"), "Должно содержать максимальный размер ответа");
        assertTrue(content.contains("500.0"), "Должно содержать средний размер ответа");
        assertTrue(content.contains("950"), "Должно содержать 95 перцентиль");
    }

    @Test
    @DisplayName("Должен правильно форматировать список файлов в общей информации")
    void shouldFormatFilesListInGeneralInfo(@TempDir Path tempDir) throws Exception {
        AnalysisContext context = createTestContext();
        Path outputFile = tempDir.resolve("files_format.adoc");

        writer.write(outputFile, context);

        String content = Files.readString(outputFile);

        assertTrue(content.contains("`access.log`"));
        assertTrue(content.contains("`http://example.com/access.log`"));
    }

    @Test
    @DisplayName("Должен отображать таблицу ресурсов с правильными данными")
    void shouldDisplayResourcesTableWithCorrectData(@TempDir Path tempDir) throws Exception {
        AnalysisContext context = createTestContext();
        Path outputFile = tempDir.resolve("resources.adoc");

        writer.write(outputFile, context);

        String content = Files.readString(outputFile);

        assertTrue(content.contains("[cols=\"3,1\", options=\"header\"]"));
        assertTrue(content.contains("| Ресурс | Количество"));

        assertTrue(content.contains("/downloads/product_1"));
        assertTrue(content.contains("/downloads/product_2"));
        assertTrue(content.contains("1000"));
        assertTrue(content.contains("100"));
    }

    @Test
    @DisplayName("Должен отображать таблицу HTTP кодов с правильными именами")
    void shouldDisplayHttpCodesTableWithCorrectNames(@TempDir Path tempDir) throws Exception {
        AnalysisContext context = createTestContext();
        Path outputFile = tempDir.resolve("http_codes.adoc");

        writer.write(outputFile, context);

        String content = Files.readString(outputFile);

        assertTrue(content.contains("[cols=\"1,2,1\", options=\"header\"]"));
        assertTrue(content.contains("| Код | Имя | Количество"));

        assertTrue(content.contains("200"));
        assertTrue(content.contains("OK"));
        assertTrue(content.contains("1000"));

        assertTrue(content.contains("401"));
        assertTrue(content.contains("Unauthorized"));
        assertTrue(content.contains("10"));

        assertTrue(content.contains("500"));
        assertTrue(content.contains("Internal Server Error"));
        assertTrue(content.contains("1"));
    }

    @Test
    @DisplayName("Должен отображать статистику по датам в правильном формате")
    void shouldDisplayDateStatisticsInCorrectFormat(@TempDir Path tempDir) throws Exception {
        AnalysisContext context = createTestContext();
        Path outputFile = tempDir.resolve("dates.adoc");

        writer.write(outputFile, context);

        String content = Files.readString(outputFile);

        assertTrue(content.contains("[cols=\"1,1,1,1\", options=\"header\"]"));
        assertTrue(content.contains("| Дата | День недели | Количество | Доля"));

        assertTrue(content.contains("2024-03-01"));
        assertTrue(content.contains("Monday"));
        assertTrue(content.contains("2981"));
        assertTrue(content.contains("12.1"));
    }

    @Test
    @DisplayName("Должен отображать таблицу протоколов")
    void shouldDisplayProtocolsTable(@TempDir Path tempDir) throws Exception {
        AnalysisContext context = createTestContext();
        Path outputFile = tempDir.resolve("protocols.adoc");

        writer.write(outputFile, context);

        String content = Files.readString(outputFile);

        assertTrue(content.contains("[cols=\"1,1\", options=\"header\"]"));
        assertTrue(content.contains("| Протокол | Количество"));

        assertTrue(content.contains("HTTP/1.1"));
        assertTrue(content.contains("HTTP/2.0"));
        assertTrue(content.contains("grpc"));
    }

    @Test
    @DisplayName("Должен обрабатывать контекст с пустыми данными")
    void shouldHandleContextWithEmptyData(@TempDir Path tempDir) throws Exception {
        AnalysisContext emptyContext = createEmptyContext();
        Path outputFile = tempDir.resolve("empty.adoc");

        writer.write(outputFile, emptyContext);

        assertTrue(Files.exists(outputFile));

        String content = Files.readString(outputFile);
        assertTrue(content.contains("= Анализ логов NGINX"));
    }

    @Test
    @DisplayName("Должен выбрасывать исключение при ошибке записи файла")
    void shouldThrowExceptionOnWriteError(@TempDir Path tempDir) {
        Path invalidPath = tempDir.resolve("nonexistent/directory/report.adoc");

        AnalysisContext context = createTestContext();

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> writer.write(invalidPath, context));

        assertTrue(exception.getMessage().contains("Не удалось записать AsciiDoc в файл"));
    }


    private AnalysisContext createTestContext() {
        AnalysisContext context = new AnalysisContext();

        context.setFiles(List.of("access.log", "http://example.com/access.log"));

        context.setStartDate(ZonedDateTime.now().minusDays(7));
        context.setEndDate(ZonedDateTime.now());

        context.setTotalRequestsCount(10000);
        context.setResponseSizeInBytes(new ResponseSize(500.0, 1000, 950));

        context.setResources(List.of(
            new Resource("/downloads/product_1", 1000),
            new Resource("/downloads/product_2", 100)
        ));

        context.setResponseCodes(List.of(
            new ResponseCode(200, 1000),
            new ResponseCode(401, 10),
            new ResponseCode(500, 1)
        ));

        context.setRequestsPerDate(List.of(
            new Date("2024-03-01", "Monday", 2981, 12.1)
        ));

        context.setUniqueProtocols(Set.of("HTTP/1.1", "HTTP/2.0", "grpc"));

        return context;
    }

    private AnalysisContext createEmptyContext() {
        AnalysisContext context = new AnalysisContext();
        context.setFiles(List.of());
        context.setTotalRequestsCount(0);
        context.setResponseSizeInBytes(new ResponseSize(0.0, 0, 0));
        context.setResources(List.of());
        context.setResponseCodes(List.of());
        context.setRequestsPerDate(List.of());
        context.setUniqueProtocols(Set.of());
        return context;
    }
}
