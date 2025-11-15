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

public class GlobProcessor {
    private static final Logger logger = LogManager.getLogger(GlobProcessor.class);

    public static List<Reader> processGlobPattern(String globPattern) throws IOException {
        List<Reader> readers = new ArrayList<>();
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + globPattern);
        Path root = extractRoot(globPattern);

        if (!Files.exists(root)) {
            throw new IOException("Директория не существует: " + root);
        }

        try (Stream<Path> stream = Files.walk(root)) {
            stream
                .filter(Files::isRegularFile)
                .filter(matcher::matches)
                .filter(p -> FileValidator.isValidLogFile(p.toString()))
                .forEach(p -> {
                    readers.add(new LocalReader(p.toString()));
                    logger.info("Создан LocalReader по glob для: {}", p);
                });
        }

        return readers;
    }

    private static Path extractRoot(String path) {
        Path pathObj = Paths.get(path);
        Path root = pathObj.isAbsolute() ? pathObj.getRoot() : Paths.get("");

        for (Path part : pathObj) {
            if (!containsGlobCharacters(part.toString())) {
                root = root.resolve(part);
            } else {
                break;
            }
        }
        return root;
    }

    private static boolean containsGlobCharacters(String path) {
        return path.contains("*") || path.contains("?") || path.contains("[") || path.contains("{");
    }
}
