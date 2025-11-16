package academy.util;

import academy.io.input.LocalReader;
import academy.io.input.Reader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Обрабатывает glob паттерны для поиска файлов.
 */
public class GlobProcessor {
    private static final Logger logger = LogManager.getLogger(GlobProcessor.class);

    /**
     * Обрабатывает glob паттерн и создает readers для найденных файлов.
     *
     * @param globPattern паттерн для поиска файлов (например, "logs/*.log")
     * @return список readers для найденных лог-файлов
     * @throws IOException если директория не существует или ошибка доступа
     */
    public static List<Reader> processGlobPattern(String globPattern) throws IOException {
        List<Reader> readers = new ArrayList<>();
        PathMatcher matcher = createPathMatcher(globPattern);
        Path root = extractRoot(globPattern);

        validateRootDirectory(root);

        List<Path> matchingFiles;
        try (Stream<Path> stream = Files.walk(root)) {
            matchingFiles = stream
                .filter(Files::isRegularFile)
                .filter(matcher::matches)
                .toList();
        }

        for (Path file : matchingFiles) {
            if (isValidLogFile(file)) {
                addReader(readers, file);
            }
        }

        return readers;
    }
    private static PathMatcher createPathMatcher(String pattern) {
        return FileSystems.getDefault().getPathMatcher("glob:" + pattern);
    }

    private static void validateRootDirectory(Path root) throws IOException {
        if (!Files.exists(root)) {
            throw new IOException("Директория не существует: " + root);
        }
    }

    private static boolean isValidLogFile(Path file) throws IOException {
        return FileValidator.isValidLogFile(file.toString());
    }

    private static void addReader(List<Reader> readers, Path file) {
        readers.add(new LocalReader(file.toString()));
        logger.debug("Добавлен файл: {}", file);
    }

    private static Path extractRoot(String path) {
        Path pathObj = Paths.get(path);
        Path root = pathObj.isAbsolute() ? pathObj.getRoot() : Paths.get("");

        for (Path part : pathObj) {
            if (containsGlobCharacters(part.toString())) {
                break;
            }
            root = root.resolve(part);
        }
        return root.normalize();
    }

    private static boolean containsGlobCharacters(String path) {
        return path.contains("*") || path.contains("?") || path.contains("[") || path.contains("{");
    }
}
