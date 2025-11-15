package academy.analytics;

import academy.cli.LogAnalyzerCommand;
import academy.io.input.Reader;
import academy.parser.LogsParser;
import academy.model.AnalysisContext;
import academy.model.ParsedLog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

public class Analyzer {
    private static final Logger logger = LogManager.getLogger(Analyzer.class);

    private final List<AnalyzerModule> analyzerModules;
    private final LogsParser logsParser;


    public Analyzer(List<AnalyzerModule> analyzerModules, LogsParser logsParser) {
        this.analyzerModules = analyzerModules;
        this.logsParser = logsParser;
    }

    public Analyzer (LogsParser logsParser) {
        this.analyzerModules = List.of( // важен порядок вызова: 1-м вызывается RequestStats, так как он сохраняет в контекст количество запросов, которое используется и в других классах
            new RequestStats(),
            new ResponseSizeStats(),
            new StatusCodeStats(),
            new TopResourcesStats(),
            new DateDistributionStats(),
            new ProtocolStats());
        this.logsParser = logsParser;
    }

    public AnalysisContext analyse(List<Reader> readers, LocalDate dateFrom, LocalDate dateTo) throws IOException {
        logger.info("Создание контекста");
        AnalysisContext context = createContext(readers, dateFrom, dateTo);

        logger.info("Парсинг и фильтрация строк");
        Stream<ParsedLog> parsedLogs = logsParser.parseAndFilterLogs(readers, dateFrom, dateTo);
        logger.info("Сбор статистики");
        analyzeLogs(parsedLogs);
        logger.info("Заполнение результатов в контекст");
        applyResultsToContext(context);

        return context;
    }

    private void analyzeLogs(Stream<ParsedLog> parsedLogs) {
        parsedLogs.forEach(log ->
            analyzerModules.forEach(module -> module.accept(log))
        );
    }

    private void applyResultsToContext(AnalysisContext context) {
        analyzerModules.forEach(module -> module.applyToContext(context));
    }

    private AnalysisContext createContext(List<Reader> readers, LocalDate dateFrom, LocalDate dateTo) {
        AnalysisContext context = new AnalysisContext();
        context.setFiles(readers.stream().map(Reader::getPath).toList());
        context.setStartDate(dateFrom);
        context.setEndDate(dateTo);

        return context;
    }
}
