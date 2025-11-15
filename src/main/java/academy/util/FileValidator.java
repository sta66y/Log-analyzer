package academy.util;

import java.nio.file.Files;
import java.nio.file.Path;

/** Валидатор файлов и путей. */
public class FileValidator {

    /**
     * Проверяет, является ли путь валидным лог-файлом.
     *
     * @param path путь к файлу
     * @return true если файл существует, является обычным файлом и имеет расширение .log или .txt
     */
    public static boolean isValidLogFile(String path) {
        Path filePath = Path.of(path);
        String fileName = filePath.getFileName().toString().toLowerCase();

        return hasValidExtension(fileName)
            && Files.exists(filePath)
            && Files.isRegularFile(filePath);
    }

    /**
     * Проверяет, является ли путь валидным URL.
     *
     * @param path путь для проверки
     * @return true если путь начинается с http:// или https://
     */
    public static boolean isValidRemoteFile(String path) {
        return path.startsWith("https://") || path.startsWith("http://");
    }

    /**
     * Проверяет, является ли путь валидным файлом (локальным или удаленным).
     *
     * @param path путь для проверки
     * @return true если путь валидный лог-файл или URL
     */
    public static boolean isValidFile(String path) {
        return isValidLogFile(path) || isValidRemoteFile(path);
    }

    private static boolean hasValidExtension(String fileName) {
        return fileName.endsWith(".log") || fileName.endsWith(".txt");
    }
}
