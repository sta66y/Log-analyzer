package academy.cli;

import academy.analytics.Analyzer;
import academy.cli.converter.OutputFormatTypeConverter;
import academy.enums.OutputFormats;
import academy.io.input.Reader;
import academy.io.output.Writer;
import academy.io.output.WriterFabric;
import academy.util.AnalysisContext;
import academy.util.ParsedLog;
import academy.parser.Parser;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static academy.io.input.ReaderFabric.createReader;

@Command(
    name = "Анализатор логов NGINX",
    version = "1.0",
    mixinStandardHelpOptions = true
)
public class LogAnalyzerCommand implements Runnable{

    private static final Logger logger = LogManager.getLogger(LogAnalyzerCommand.class);
    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z"); //TODO вынести в конфиг

    @Option(
        names = {"-p", "--path"},
        description = "Путь к одному или нескольким NGINX лог-файлам (файлы указывать через пробел)",
        required = true
    )
    private List<String> paths;

    @Option(
        names = {"-f", "--format"},
        converter = OutputFormatTypeConverter.class,
        description = "Формат вывода результатов: ${COMPLETION-CANDIDATES}",
        required = false
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
            validateDates();

            Stream<String> combinedStream = paths.stream()
                .flatMap(path -> {
                    Reader reader = createReader(path);
                    return reader.read(path);
                });

            Parser parser = new Parser();
            Stream<ParsedLog> parsedLogStream = parser.parse(combinedStream);

            Analyzer analyzer = new Analyzer(dateFrom, dateTo);
            AnalysisContext context = analyzer.analyseLog(parsedLogStream);
            context.setFiles(paths); //TODO подумать...
            context.setEndDate(dateFrom);
            context.setEndDate(dateTo);

            Writer writer = WriterFabric.createWriter(format); //TODO мб пикокли конверт сразу в writer
            writer.write(output, context);

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
