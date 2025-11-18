package academy.io.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class LocalReaderTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Должен возвращать поток строк, если файл существует")
    void read_ShouldReturnStreamString_WhenFileExists() throws IOException, InterruptedException {
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
        IOException ex = assertThrows(IOException.class, () -> reader.read());

        assertTrue(ex.getMessage().contains("Ошибка при чтении файла"));
    }
}
