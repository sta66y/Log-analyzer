package academy.cli;

import academy.cli.converter.OutputFormatTypeConverter;
import academy.io.Reader;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static academy.io.ReaderFabric.createReader;

@Command(
    name = "Анализатор логов NGINX",
    version = "1.0",
    mixinStandardHelpOptions = true
)
public class LogAnalyzerCommand implements Runnable{

    private static final Logger logger = LogManager.getLogger(LogAnalyzerCommand.class);

    @Option(
        names = {"-p", "--path"},
        description = "Путь к одному или нескольким NGINX лог-файлам",
        required = true
    )
    private String path;

    @Option(
        names = {"-f", "--format"},
        converter = OutputFormatTypeConverter.class,
        description = "Формат вывода результатов: ${COMPLETION-CANDIDATES}",
        required = false
    )
    private String format;

    @Option(
        names = {"-o", "--output"},
        description = "Путь до файла, куда должен быть сохранён результат работы программы",
        required = true
    )
    private String output;

    @Option(
        names = {"--from"},
        description = "Дата, с которой начать анализ. Формат ISO8601",
        required = false
    )
    private String dateFrom;

    @Option(
        names = {"--to"},
        description = "Дата, до которой начать анализ. Формат ISO8601",
        required = false
    )
    private String dateTo;




    @Override
    public void run() {
        try {
            validatePath(path);

            Reader reader = createReader(path);
            Stream<String> stream = reader.read(path);


        } catch (Exception e) {
            System.exit(2);
        }

        //TODO проверить что формат либо .txt, либо .log
        //TODO вынести ошибки, если не удалось открыть файл

        //TODO проверить from и to

        //TODO аналитика

        //TODO класс для вывода результатов в формате format

        //TODO запись файла
        //TODO ошибка если файл существует, директория недоступна, расширение не соответствует


    }

    private void validatePath(String path) {
        if (!Files.exists(Path.of(path))) {
            logger.error("Такого файла не существует: {}", path);
            throw new IllegalArgumentException("Такого файла не существует: " + path);
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new LogAnalyzerCommand()).execute(args);
        System.exit(exitCode);
    }

}
