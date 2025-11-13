package academy.io.input;

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
    private final Reader reader = new LocalReader();

    @TempDir
    Path tempDir;

    @Test
    void read_ShouldReturnStreamString_WhenFileExists() throws IOException {
        Path file = tempDir.resolve("test.log");
        List<String> lines = List.of("line1", "line2", "line3");
        Files.write(file, lines);

        Stream<String> stream = reader.read(file.toString());

        List<String> readLines = stream.collect(Collectors.toList());
        assertEquals(lines, readLines);
    }

    @Test
    void read_ShouldThrowException_WhenFileDoesntExists() {
        Path file = tempDir.resolve("test.log");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> reader.read(file.toString()));

        assertEquals("Ошибка при чтении файла", ex.getMessage());
    }
}
