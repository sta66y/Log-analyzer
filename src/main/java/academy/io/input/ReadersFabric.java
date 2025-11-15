package academy.io.input;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ReadersFabric {
    public static List<Reader> createReaders(List<String> paths) {
        List<Reader> readers = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (String path : paths) {
            try {
                if (path.startsWith("http")) {
                    readers.add(new RemoteReader(path));
                } else if (containsGlobCharacters(path)) {
                    processGlobPattern(path, readers);
                } else if (isValidLogFile(path)) {
                    readers.add(new LocalReader(path));
                } else {
                    errors.add("Неподдерживаемый формат файла: " + path);
                }
            } catch (Exception e) {
                errors.add("Ошибка обработки пути '" + path + "': " + e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            throw new RuntimeException("Ошибки при чтении файлов:\n" + String.join("\n", errors));
        }

        if (readers.isEmpty()) {
            throw new RuntimeException("Подходящих файлов не обнаружено");
        }

        return readers;
    }

    private static boolean isValidLogFile(String path) {
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
                .forEach(p -> readers.add(new LocalReader(p.toString())));
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
