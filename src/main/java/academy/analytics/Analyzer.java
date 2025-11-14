package academy.analytics;

import academy.util.AnalysisContext;
import academy.util.ParsedLog;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

public class Analyzer {
    private final AnalysisContext context;
    private final LocalDate dateFrom;
    private final LocalDate dateTo;
    private final List<AnalyzerModule> analyzerModules;

    public Analyzer(AnalysisContext context, LocalDate dateFrom, LocalDate dateTo, List<AnalyzerModule> analyzerModules) {
        this.context = context;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.analyzerModules = analyzerModules;
    }

    public Analyzer(AnalysisContext context, LocalDate dateFrom, LocalDate dateTo) {
        this.context = context;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;

        analyzerModules = List.of( // важен порядок вызова: 1-м вызывается RequestStats, так как он сохраняет в контекст количество запросов, которое используется и в других классах
            new RequestStats(),
            new ResponseSizeStats(),
            new StatusCodeStats(),
            new TopResourcesStats(),
            new DateDistributionStats(),
            new ProtocolStats()
        );
    }

    public void analyseLog(Stream<ParsedLog> parsedLogStream) {
        parsedLogStream
            .filter(this::isWithinDateRange)
            .forEach(line -> analyzerModules.forEach(module -> module.accept(line)));

        analyzerModules
            .forEach(module -> module.applyToContext(context));
    }

    private boolean isWithinDateRange(ParsedLog log) {
        LocalDate logDate = log.date().toLocalDate();

        if (dateFrom != null && logDate.isBefore(dateFrom)) return false;
        if (dateTo != null && logDate.isAfter(dateTo)) return false;
        return true;
    }
}
