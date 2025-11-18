package academy.util;

import academy.enums.OutputFormats;
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
        if (path == null || path.trim().isEmpty()) {
            throw new IOException("Путь не может быть пустым или null");
        }

        Path filePath = Path.of(path);
        Path fileNamePath = filePath.getFileName();

        if (fileNamePath == null) {
            throw new IOException("Некорректный путь: " + path);
        }

        String fileName = fileNamePath.toString().toLowerCase();

        if (!Files.exists(filePath)) throw new IOException("Файла не существует");
        if (!Files.isRegularFile(filePath)) throw new IOException("не является файлом");

        return hasValidInputExtension(fileName);
    }

    public static void isValidOutputPath(Path filePath, OutputFormats format) throws IOException {
        if (filePath == null || format == null) {
            throw new IOException("Путь или формат не могут быть null");
        }

        Path fileNamePath = filePath.getFileName();
        if (fileNamePath == null) {
            throw new IOException("Некорректный путь: " + filePath);
        }

        if (!hasValidOutputExtension(fileNamePath.toString(), format)) {
            throw new IOException("Несоответствие форматов");
        }
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

    private static boolean hasValidInputExtension(String fileName) {
        return fileName.endsWith(".log") || fileName.endsWith(".txt");
    }

    private static boolean hasValidOutputExtension(String fileName, OutputFormats formats) {
        return switch (formats) {
            case ADOC -> fileName.endsWith(".adoc");
            case MD -> fileName.endsWith(".md");
            case JSON -> fileName.endsWith(".json");
        };
    }
}
