package academy.cli;

import academy.analytics.Analyzer;
import academy.cli.converter.OutputFormatTypeConverter;
import academy.enums.OutputFormats;
import academy.io.input.Reader;
import academy.io.input.ReadersFactory;
import academy.io.output.Writer;
import academy.io.output.WriterFabric;
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

@Command(
    name = "Анализатор логов NGINX",
    version = "1.0",
    mixinStandardHelpOptions = true
)
public class LogAnalyzerCommand implements Runnable{

    private static final Logger logger = LogManager.getLogger(LogAnalyzerCommand.class);

    @Option(
        names = {"-p", "--path"},
        description = "Путь к NGINX лог-файлу",
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
        description = "Путь до файла, куда должен быть сохранён результат работы программы",
        required = true
    )
    private Path output;

    @Option(
        names = {"--from"},
        description = "Дата, с которой начать анализ. Формат ISO8601",
        required = false
    )
    private LocalDate dateFrom;

    @Option(
        names = {"--to"},
        description = "Дата, до которой завершить анализ. Формат ISO8601",
        required = false
    )
    private LocalDate dateTo;

    @Override
    public void run() {
        try {
            logger.info("Валидация dateFrom и dateTo");
            validateDates();

            logger.info("Создание reader для каждого файла");
            List<Reader> readers = ReadersFactory.createReaders(paths);

            PathParser pathParser = new PathParser();
            LogsParser logsParser = new LogsParser(pathParser);

            logger.info("Создание analyzer");
            Analyzer analyzer = new Analyzer(logsParser);
            AnalysisContext context = analyzer.analyse(readers, dateFrom, dateTo);

            logger.info("Создание writer по {}", format);
            Writer writer = WriterFabric.createWriter(format);

            logger.info("Запись результатов анализа в файл");
            writer.write(output, context);
            logger.info("Результат успешно записан в файл {} в формате {}", output, format);

        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(2);
        }
    }

    private void validateDates() {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            logger.error("--from не может быть позже, чем --to");
            throw new IllegalArgumentException("--from не может быть позже, чем --to");
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new LogAnalyzerCommand()).execute(args);
        System.exit(exitCode);
    }
}
