package academy.util;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileValidator {

    public static boolean isValidLogFile(String path) {
        Path filePath = Path.of(path);
        String fileName = filePath.getFileName().toString().toLowerCase();

        return (fileName.endsWith(".log") || fileName.endsWith(".txt"))
            && Files.exists(filePath)
            && Files.isRegularFile(filePath);
    }

    public static boolean isValidRemoteFile(String path) {
        return path.startsWith("https://") || path.startsWith("http://");
    }

    public static boolean isValidFile(String path) {
        return isValidLogFile(path) || isValidRemoteFile(path);
    }
}
