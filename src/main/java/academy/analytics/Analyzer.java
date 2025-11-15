package academy.analytics;

import academy.io.input.Reader;
import academy.parser.Parser;
import academy.util.AnalysisContext;
import academy.util.ParsedLog;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

public class Analyzer {
    private final List<AnalyzerModule> analyzerModules;
    private final Parser parser;


    public Analyzer (List<AnalyzerModule> analyzerModules, Parser parser) {
        this.analyzerModules = analyzerModules;
        this.parser = parser;
    }

    public Analyzer () {
        this.analyzerModules = List.of( // важен порядок вызова: 1-м вызывается RequestStats, так как он сохраняет в контекст количество запросов, которое используется и в других классах
            new RequestStats(),
            new ResponseSizeStats(),
            new StatusCodeStats(),
            new TopResourcesStats(),
            new DateDistributionStats(),
            new ProtocolStats());
        this.parser = new Parser();
    }

    public AnalysisContext analyse(List<Reader> readers, LocalDate dateFrom, LocalDate dateTo) {
        AnalysisContext context = createContext(readers, dateFrom, dateTo);

        Stream<ParsedLog> parsedLogs = parseAndFilterLogs(readers, dateFrom, dateTo);
        processLogs(parsedLogs);
        applyResultsToContext(context);

        return context;
    }

    private Stream<ParsedLog> parseAndFilterLogs(List<Reader> readers, LocalDate dateFrom, LocalDate dateTo) {
        return readers.stream()
            .flatMap(Reader::read)
            .map(parser::parseLine)
            .filter(parsedLog -> isWithinDateRange(parsedLog, dateFrom, dateTo));
    }

    private void processLogs(Stream<ParsedLog> parsedLogs) {
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

    private boolean isWithinDateRange(ParsedLog log, LocalDate dateFrom, LocalDate dateTo) {
        LocalDate logDate = log.date().toLocalDate();

        if (dateFrom != null && logDate.isBefore(dateFrom)) return false;
        if (dateTo != null && logDate.isAfter(dateTo)) return false;
        return true;
    }
}
