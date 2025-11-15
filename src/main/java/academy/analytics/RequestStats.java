package academy.analytics;

import academy.model.AnalysisContext;
import academy.model.ParsedLog;

/** Собирает статистику общего количества запросов */
public class RequestStats implements AnalyzerModule {
    private int cntRequests = 0;

    @Override
    public void accept(ParsedLog log) {
        incrementCount();
    }

    @Override
    public void applyToContext(AnalysisContext context) {
        context.setTotalRequestsCount(cntRequests);
    }

    private void incrementCount() {
        cntRequests++;
    }
}
