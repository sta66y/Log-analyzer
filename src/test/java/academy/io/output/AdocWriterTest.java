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

class AdocWriterTest {
    private final Writer writer = new AdocWriter();

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Должен создавать AsciiDoc файл с правильной структурой")
    void shouldCreateAsciiDocFileWithCorrectStructure() throws Exception {
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
    void shouldDisplayGeneralInformationInTable() throws Exception {
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
    void shouldFormatFilesListInGeneralInfo() throws Exception {
        AnalysisContext context = createTestContext();
        Path outputFile = tempDir.resolve("files_format.adoc");

        writer.write(outputFile, context);

        String content = Files.readString(outputFile);

        assertTrue(content.contains("`access.log`"));
        assertTrue(content.contains("`http://example.com/access.log`"));
    }

    @Test
    @DisplayName("Должен отображать таблицу ресурсов с правильными данными")
    void shouldDisplayResourcesTableWithCorrectData() throws Exception {
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
    void shouldDisplayHttpCodesTableWithCorrectNames() throws Exception {
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
    void shouldDisplayDateStatisticsInCorrectFormat() throws Exception {
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
    void shouldDisplayProtocolsTable() throws Exception {
        AnalysisContext context = createTestContext();
        Path outputFile = tempDir.resolve("protocols.adoc");

        writer.write(outputFile, context);

        String content = Files.readString(outputFile);

        assertTrue(content.contains("[cols=\"1,1\", options=\"header\"]"));
        assertTrue(content.contains("| Протокол"));

        assertTrue(content.contains("HTTP/1.1"));
        assertTrue(content.contains("HTTP/2.0"));
        assertTrue(content.contains("grpc"));
    }

    @Test
    @DisplayName("Должен обрабатывать контекст с пустыми данными")
    void shouldHandleContextWithEmptyData() throws Exception {
        AnalysisContext emptyContext = createEmptyContext();
        Path outputFile = tempDir.resolve("empty.adoc");

        writer.write(outputFile, emptyContext);

        assertTrue(Files.exists(outputFile));

        String content = Files.readString(outputFile);
        assertTrue(content.contains("= Анализ логов NGINX"));
    }

    @Test
    @DisplayName("Должен выбрасывать исключение при ошибке записи файла")
    void shouldThrowExceptionOnWriteError() {
        Path invalidPath = tempDir.resolve("nonexistent/directory/report.adoc");

        AnalysisContext context = createTestContext();

        IOException exception = assertThrows(IOException.class, () -> writer.write(invalidPath, context));

        assertTrue(exception.getMessage().contains("Не удалось записать AsciiDoc в файл"));
    }
}
