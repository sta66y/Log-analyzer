package academy.cli;

import academy.analytics.Analyzer;
import academy.cli.converter.OutputFormatTypeConverter;
import academy.enums.OutputFormats;
import academy.io.input.LocalReader;
import academy.io.input.Reader;
import academy.io.input.RemoteReader;
import academy.io.output.Writer;
import academy.io.output.WriterFabric;
import academy.util.AnalysisContext;
import academy.util.ParsedLog;
import academy.parser.Parser;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;
import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

            AnalysisContext context = new AnalysisContext();
            context.setStartDate(dateFrom);
            context.setEndDate(dateTo);

            List<Reader> readers = getReaders(paths);

            List<String> files = readers.stream()
                .map(Reader::getPath)
                .toList();

            context.setFiles(files);

            Stream<String> combinedStream = readers.stream()
                .flatMap(Reader::read);


            Parser parser = new Parser();
            Stream<ParsedLog> parsedLogStream = parser.parse(combinedStream);

            context.setStartDate(dateFrom);
            context.setEndDate(dateTo);

            Analyzer analyzer = new Analyzer(context, dateFrom, dateTo);
            analyzer.analyseLog(parsedLogStream);

            Writer writer = WriterFabric.createWriter(format);
            writer.write(output, context);

        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(2);
        }

    }

    private List<Reader> getReaders(List<String> paths) {
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

    private boolean isValidLogFile(String path) {
        String lowerPath = path.toLowerCase();
        return (lowerPath.endsWith(".log") || lowerPath.endsWith(".txt"))
            && Files.exists(Paths.get(path))
            && Files.isRegularFile(Paths.get(path));
    }

    private void processGlobPattern(String globPattern, List<Reader> readers) throws IOException {
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

    private Path extractRoot(String path) {
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

    private boolean containsGlobCharacters(String path) {
        return path.contains("*") || path.contains("?") || path.contains("[") || path.contains("{");
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
//TODO toLowerCase сделать
