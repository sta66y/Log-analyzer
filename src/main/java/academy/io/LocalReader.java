package academy.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LocalReader implements Reader{
    private static final Logger logger = LogManager.getLogger(LocalReader.class);

    @Override
    public Stream<String> read(String path) {
        try {
            return Files.lines(Path.of(path));
        } catch (IOException e) {
            logger.fatal("Ошибка при чтении файла");
            throw new RuntimeException("Ошибка при чтении файла");
        }
    }
}
