package academy.io.input;

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


public class ReadersFactory {
    private static final Logger logger = LogManager.getLogger(ReadersFactory.class);

    public static List<Reader> createReaders(List<String> paths) throws IOException {
        List<Reader> readers = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (String path : paths) {
            try {
                if (path.startsWith("http")) {
                    readers.add(new RemoteReader(path));
                    logger.info("Добавлен reader для {}", path);
                } else if (containsGlobCharacters(path)) {
                    logger.info("Начата обработка glob: {}", path);
                    processGlobPattern(path, readers);
                } else if (isValidLogFile(path)) {
                    readers.add(new LocalReader(path));
                    logger.info("Добавлен reader: {}", path);
                } else {
                    errors.add("Неподдерживаемый формат файла: " + path);
                }
            } catch (IOException e) {
                errors.add("Ошибка обработки пути " + path + ": " + e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            throw new IOException("Ошибки при чтении файлов:\n" + String.join("\n", errors));
        }

        if (readers.isEmpty()) {
            throw new IOException("Подходящих файлов не обнаружено"); //TODO может другая ex
        }

        return readers;
    }

    private static boolean isValidLogFile(String path) {
        logger.info("Валидация файла {}", path);
        String lowerPath = path.toLowerCase();
        return (lowerPath.endsWith(".log") || lowerPath.endsWith(".txt"))
            && Files.exists(Paths.get(path))
            && Files.isRegularFile(Paths.get(path));
    }

    private static void processGlobPattern(String globPattern, List<Reader> readers) throws IOException {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + globPattern);
        Path root = extractRoot(globPattern);

        if (!Files.exists(root)) {
            throw new IOException("Директория не существует: " + root);
        }

        try (Stream<Path> stream = Files.walk(root)) {
            stream
                .filter(Files::isRegularFile)
                .filter(matcher::matches)
                .filter(p -> {
                    String name = p.toString().toLowerCase();
                    return name.endsWith(".log") || name.endsWith(".txt");
                })
                .forEach(p -> {
                    readers.add(new LocalReader(p.toString()));
                    logger.info("Добавлен reader: {}", p);}
                );
        }
    }

    private static Path extractRoot(String path) {
        Path pathObj = Paths.get(path);
        Path root = Paths.get("/");

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

//TODO может разделить?
