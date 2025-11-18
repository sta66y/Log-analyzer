package academy.cli;

import academy.analytics.Analyzer;
import academy.cli.converter.OutputFormatTypeConverter;
import academy.enums.OutputFormats;
import academy.io.input.Reader;
import academy.io.output.Writer;
import academy.model.AnalysisContext;
import academy.parser.LogsParser;
import academy.parser.PathParser;
import academy.util.FileValidator;
import academy.util.ReadersCreator;
import academy.util.WriterFactory;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/** Командный интерфейс для анализатора логов NGINX. Обрабатывает аргументы командной строки и запускает анализ. */
@Command(name = "Анализатор логов NGINX", version = "1.0", mixinStandardHelpOptions = true)
public class LogAnalyzerCommand implements Callable<Integer> {

    private static final Logger logger = LogManager.getLogger(LogAnalyzerCommand.class);

    @Option(
            names = {"-p", "--path"},
            description = "Путь к NGINX лог-файлу или URL",
            required = true)
    private List<String> paths;

    @Option(
            names = {"-f", "--format"},
            converter = OutputFormatTypeConverter.class,
            description = "Формат вывода результатов: ${COMPLETION-CANDIDATES}",
            required = true)
    private OutputFormats format;

    @Option(
            names = {"-o", "--output"},
            description = "Путь для сохранения результата",
            required = true)
    private Path output;

    @Option(
            names = {"--from"},
            description = "Начальная дата анализа (ISO8601)",
            required = false)
    private LocalDate dateFrom;

    @Option(
            names = {"--to"},
            description = "Конечная дата анализа (ISO8601)",
            required = false)
    private LocalDate dateTo;

    /** Запускает процесс анализа логов. Читает логи, анализирует их и сохраняет результат в указанном формате. */
    @Override
    public Integer call() {
        try {
            logger.info("Запуск анализатора логов");
            validateDates();

            logger.info("Создание readers для {} путей", paths.size());
            List<Reader> readers = ReadersCreator.createReaders(paths);

            logger.info("Запуск анализа");
            Analyzer analyzer = new Analyzer(new LogsParser(new PathParser()));
            AnalysisContext context = analyzer.analyse(readers, dateFrom, dateTo);

            logger.info("Проверка output");
            FileValidator.isValidOutputPath(output, format);

            logger.info("Сохранение результатов в формате {}", format);
            Writer writer = WriterFactory.createWriter(format);
            writer.write(output, context);

            logger.info("Анализ завершен. Результат сохранен в: {}", output);
            return 0;
        } catch (IOException | RuntimeException e) {
            logger.fatal("Ошибка выполнения: {}", e.getMessage());
            return 2;
        } catch (Exception e) {
            logger.fatal("Непредвиденная ошибка", e);
            return 1;
        }
    }

    /**
     * Проверяет корректность диапазона дат.
     *
     * @throws IllegalArgumentException если дата начала позже даты окончания
     */
    private void validateDates() {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("Дата начала не может быть позже даты окончания");
        }
    }
}
