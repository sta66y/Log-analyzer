package academy.analytics;

import academy.model.AnalysisContext;
import academy.model.ParsedLog;

/** Собирает статистику общего количества запросов */
public class AnalyzeTotalCountRequests implements AnalyzerModule {
    private int countTotalRequests = 0;

    @Override
    public void accept(ParsedLog log) {
        incrementCount();
    }

    @Override
    public void applyToContext(AnalysisContext context) {
        context.setTotalRequestsCount(countTotalRequests);
    }

    private void incrementCount() {
        countTotalRequests++;
    }
}
