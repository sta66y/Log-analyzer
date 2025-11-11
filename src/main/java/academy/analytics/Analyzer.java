package academy.analytics;

import academy.util.AnalysisContext;
import academy.util.ParsedLog;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

public class Analyzer {
    //TODO обработать случай, когда log пустой

    private final ZonedDateTime dateFrom;
    private final ZonedDateTime dateTo;

    private final List<AnalyzerModule> analyzerModules;

    public Analyzer(ZonedDateTime dateFrom, ZonedDateTime dateTo, List<AnalyzerModule> analyzerModules) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;

        this.analyzerModules = analyzerModules;
    }

    public Analyzer(ZonedDateTime dateFrom, ZonedDateTime dateTo) {
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

    public AnalysisContext analyseLog(Stream<ParsedLog> parsedLogStream) {
        AnalysisContext context = new AnalysisContext();

        parsedLogStream
            .filter(this::isWithinDateRange)
            .forEach(line -> analyzerModules.forEach(module -> module.accept(line)));

        analyzerModules
            .forEach(module -> module.applyToContext(context));

        return context;
    }

    private boolean isWithinDateRange(ParsedLog log) {
        if (dateFrom != null && log.date().isBefore(dateFrom)) return false;
        if (dateTo != null && log.date().isAfter(dateTo)) return false;
        return true;
    }
}
