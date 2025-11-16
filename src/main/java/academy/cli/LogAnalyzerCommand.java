package academy.cli;

import academy.analytics.Analyzer;
import academy.cli.converter.OutputFormatTypeConverter;
import academy.enums.OutputFormats;
import academy.io.input.Reader;
import academy.io.output.Writer;
import academy.util.ReadersCreator;
import academy.util.WriterFactory;
import academy.model.AnalysisContext;
import academy.parser.LogsParser;
import academy.parser.PathParser;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Командный интерфейс для анализатора логов NGINX.
 * Обрабатывает аргументы командной строки и запускает анализ.
 */
@Command(
    name = "Анализатор логов NGINX",
    version = "1.0",
    mixinStandardHelpOptions = true
)
public class LogAnalyzerCommand implements Runnable {

    private static final Logger logger = LogManager.getLogger(LogAnalyzerCommand.class);

    @Option(
        names = {"-p", "--path"},
        description = "Путь к NGINX лог-файлу или URL",
        required = true
    )
    private List<String> paths;

    @Option(
        names = {"-f", "--format"},
        converter = OutputFormatTypeConverter.class,
        description = "Формат вывода результатов: ${COMPLETION-CANDIDATES}",
        required = true
    )
    private OutputFormats format;

    @Option(
        names = {"-o", "--output"},
        description = "Путь для сохранения результата",
        required = true
    )
    private Path output;

    @Option(
        names = {"--from"},
        description = "Начальная дата анализа (ISO8601)",
        required = false
    )
    private LocalDate dateFrom;

    @Option(
        names = {"--to"},
        description = "Конечная дата анализа (ISO8601)",
        required = false
    )
    private LocalDate dateTo;

    /**
     * Запускает процесс анализа логов.
     * Читает логи, анализирует их и сохраняет результат в указанном формате.
     */
    @Override
    public void run() {
        try {
            logger.info("Запуск анализатора логов");
            validateDates();

            logger.info("Создание readers для {} путей", paths.size());
            List<Reader> readers = ReadersCreator.createReaders(paths);

            logger.info("Запуск анализа");
            Analyzer analyzer = new Analyzer(new LogsParser(new PathParser()));
            AnalysisContext context = analyzer.analyse(readers, dateFrom, dateTo);

            logger.info("Сохранение результатов в формате {}", format);
            Writer writer = WriterFactory.createWriter(format);
            writer.write(output, context);

            logger.info("Анализ завершен. Результат сохранен в: {}", output);

        } catch (Exception e) {
            logger.fatal("Ошибка при выполнении анализа: {}", e.getMessage());
            System.exit(2);
        }
    }

    /**
     * Проверяет корректность диапазона дат.
     * @throws IllegalArgumentException если дата начала позже даты окончания
     */
    private void validateDates() {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new IllegalArgumentException("Дата начала не может быть позже даты окончания");
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new LogAnalyzerCommand()).execute(args);
        System.exit(exitCode);
    }
}
