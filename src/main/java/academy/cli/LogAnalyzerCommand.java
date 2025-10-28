package academy.cli;

import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;

@Command(
    name = "Анализатор логов NGINX",
    version = "1.0",
    mixinStandardHelpOptions = true
)
public class LogAnalyzerCommand implements Runnable{

    @Option(
        names = {"-p", "--path"},
        description = "Путь к одному или нескольким NGINX лог-файлам",
        required = true
    )
    private String path;

    @Option(
        names = {"-f", "--format"},
        description = "Формат вывода результатов: json | markdown | adoc",
        required = false,
        defaultValue = "json"
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
        //TODO локальный файл
        //TODO удаленный файл
        //TODO проверить что формат либо .txt, либо .log
        //TODO вынести ошибки, если не удалось открыть файл

        //TODO проверить from и to

        //TODO аналитика

        //TODO класс для вывода результатов в формате format

        //TODO запись файла
        //TODO ошибка если файл существует, директория недоступна, расширение не соответствует


    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new LogAnalyzerCommand()).execute(args);
        System.exit(exitCode);
    }

}
