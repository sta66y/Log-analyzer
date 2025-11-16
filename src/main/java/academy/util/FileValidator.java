package academy.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Валидатор файлов и путей. */
public class FileValidator {

    /**
     * Проверяет, является ли путь валидным лог-файлом.
     *
     * @param path путь к файлу
     * @throws IOException если файла не существует или он им не является
     * @return true если файл существует, является обычным файлом и имеет расширение .log или .txt
     */
    public static boolean isValidLogFile(String path) throws IOException {
        Path filePath = Path.of(path);
        String fileName = filePath.getFileName().toString().toLowerCase();

        if (!Files.exists(filePath)) throw new IOException("Файла не существует");
        if (!Files.isRegularFile(filePath)) throw new IOException("не  является файлом");

        return hasValidExtension(fileName);
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
    public static boolean isValidFile(String path) throws IOException {
        return isValidRemoteFile(path) || isValidLogFile(path);
    }

    private static boolean hasValidExtension(String fileName) {
        return fileName.endsWith(".log") || fileName.endsWith(".txt");
    }
}
