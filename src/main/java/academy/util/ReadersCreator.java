package academy.util;

import academy.io.input.Reader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Создает readers для различных типов путей.
 * Обрабатывает локальные файлы, URL и glob паттерны.
 * Собирает все ошибки обработки и выбрасывает их одним исключением.
 */
public class ReadersCreator {
    private static final Logger logger = LogManager.getLogger(ReadersCreator.class);

    /**
     * Создает readers для указанных путей.
     *
     * @param paths список путей к файлам, URL или glob паттернов
     * @return список созданных readers
     * @throws IOException если пути невалидны или не найдено подходящих файлов
     */
    public static List<Reader> createReaders(List<String> paths) throws IOException {
        List<Reader> readers = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (String path : paths) {
            try {
                if (containsGlobCharacters(path)) {
                    logger.info("Обработка glob паттерна: {}", path);
                    readers.addAll(GlobProcessor.processGlobPattern(path));
                } else if (FileValidator.isValidFile(path)) {
                    readers.add(ReaderFactory.createReader(path));
                } else {
                    errors.add("Неподдерживаемый формат: " + path);
                }
            } catch (IOException e) {
                errors.add("Ошибка обработки " + path + ": " + e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            throw new IOException("Ошибки при создании readers:\n" + String.join("\n", errors));
        }

        if (readers.isEmpty()) {
            throw new IOException("Не найдено подходящих файлов для обработки");
        }

        logger.info("Успешно создано {} readers", readers.size());
        return readers;
    }

    /** Проверяет содержит ли путь glob символы. */
    private static boolean containsGlobCharacters(String path) {
        return path.contains("*") || path.contains("?") || path.contains("[") || path.contains("{");
    }
}
