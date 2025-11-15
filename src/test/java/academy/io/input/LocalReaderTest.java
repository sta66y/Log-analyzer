package academy.io.input;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LocalReaderTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Должен возвращать поток строк, если файл существует")
    void read_ShouldReturnStreamString_WhenFileExists() throws IOException {
        Path file = tempDir.resolve("test.log");
        List<String> lines = List.of("line1", "line2", "line3");
        Files.write(file, lines);

        Reader reader = new LocalReader(file.toString());
        Stream<String> stream = reader.read();

        List<String> readLines = stream.collect(Collectors.toList());
        assertEquals(lines, readLines);
    }

    @Test
    @DisplayName("Должен выбрасывать ошибку, если файла не существует")
    void read_ShouldThrowException_WhenFileDoesntExists() {
        Path file = tempDir.resolve("test.log");

        Reader reader = new LocalReader(file.toString());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> reader.read());

        assertEquals("Ошибка при чтении файла", ex.getMessage());
    }
}
