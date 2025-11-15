package academy.io.input;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReadersFabricTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Должен создать RemoteReader для HTTP URL")
    void createReaders_ShouldCreateRemoteReaderForHttpUrl() {
        List<String> paths = List.of("http://example.com/");

        List<Reader> readers = ReadersFabric.createReaders(paths);

        assertEquals(1, readers.size());
        assertInstanceOf(RemoteReader.class, readers.get(0));
        assertEquals("http://example.com/", readers.get(0).getPath());
    }

    @Test
    @DisplayName("Должен создать RemoteReader для HTTPS URL")
    void createReaders_ShouldCreateRemoteReaderForHttpsUrl() {
        List<String> paths = List.of("https://example.com/");

        List<Reader> readers = ReadersFabric.createReaders(paths);

        assertEquals(1, readers.size());
        assertInstanceOf(RemoteReader.class, readers.get(0));
    }

    @Test
    @DisplayName("Должен создать LocalReader для существующего .log файла")
    void createReaders_ShouldCreateLocalReaderForExistingLogFile() throws IOException {
        Path logFile = tempDir.resolve("access.log");
        Files.createFile(logFile);
        List<String> paths = List.of(logFile.toString());

        List<Reader> readers = ReadersFabric.createReaders(paths);

        assertEquals(1, readers.size());
        assertInstanceOf(LocalReader.class, readers.get(0));
        assertEquals(logFile.toString(), readers.get(0).getPath());
    }

    @Test
    @DisplayName("Должен создать LocalReader для существующего .txt файла")
    void createReaders_ShouldCreateLocalReaderForExistingTxtFile() throws IOException {
        Path txtFile = tempDir.resolve("logs.txt");
        Files.createFile(txtFile);
        List<String> paths = List.of(txtFile.toString());

        List<Reader> readers = ReadersFabric.createReaders(paths);

        assertEquals(1, readers.size());
        assertInstanceOf(LocalReader.class, readers.get(0));
    }

    @Test
    @DisplayName("Должен обработать glob паттерн с несколькими файлами")
    void createReaders_ShouldProcessGlobPatternWithMultipleFiles() throws IOException {
        Files.createFile(tempDir.resolve("access.log"));
        Files.createFile(tempDir.resolve("error.log"));
        Files.createFile(tempDir.resolve("app.log"));
        Files.createFile(tempDir.resolve("config.conf")); // не должен быть выбран

        String globPattern = tempDir.toString() + "/*.log";

        List<Reader> readers = ReadersFabric.createReaders(List.of(globPattern));

        assertEquals(3, readers.size());
        assertTrue(readers.stream().allMatch(r -> r instanceof LocalReader));
        assertTrue(readers.stream().allMatch(r -> r.getPath().endsWith(".log")));
    }

    @Test
    @DisplayName("Должен обработать glob паттерн с вложенными директориями")
    void createReaders_ShouldProcessGlobPatternWithSubdirectories() throws IOException {
        Path subDir = tempDir.resolve("logs");
        Files.createDirectories(subDir);
        Files.createFile(subDir.resolve("access.log"));
        Files.createFile(subDir.resolve("error.log"));

        String globPattern = tempDir.toString() + "/**/*.log";

        List<Reader> readers = ReadersFabric.createReaders(List.of(globPattern));

        assertEquals(2, readers.size());
        assertTrue(readers.stream().allMatch(r -> r instanceof LocalReader));
    }

    @Test
    @DisplayName("Должен выбросить исключение при несуществующей директории для glob")
    void createReaders_ShouldThrowExceptionForNonExistentGlobDirectory() {
        String globPattern = "/non/existent/dir/*.log";

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> ReadersFabric.createReaders(List.of(globPattern)));

        assertTrue(exception.getMessage().contains("Директория не существует"));
    }

    @Test
    @DisplayName("Должен выбросить при неподдерживаемом формате файла")
    void createReaders_ShouldThrowExceptionForUnsupportedFileFormat() throws IOException {
        Path unsupportedFile = tempDir.resolve("data.csv");
        Files.createFile(unsupportedFile);
        List<String> paths = List.of(unsupportedFile.toString());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> ReadersFabric.createReaders(paths));

        assertTrue(exception.getMessage().contains("Неподдерживаемый формат файла"));
    }

    @Test
    @DisplayName("Должен выбросить при пустом списке путей")
    void createReaders_ShouldThrowExceptionForEmptyPaths() {
        List<String> paths = List.of();

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> ReadersFabric.createReaders(paths));

        assertTrue(exception.getMessage().contains("Подходящих файлов не обнаружено"));
    }

    @Test
    @DisplayName("Исключение при null путях")
    void createReaders_ShouldThrowExceptionForNullPaths() {
        assertThrows(NullPointerException.class,
            () -> ReadersFabric.createReaders(null));
    }
}
