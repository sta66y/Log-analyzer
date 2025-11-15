package academy.analytics;

import academy.model.AnalysisContext;
import academy.model.ResponseSize;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static academy.analytics.TestConstants.EXAMPLES_LOG;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResponseSizeStatsTest {
    private AnalyzerModule analyzer = new ResponseSizeStats();
    private AnalysisContext context = new AnalysisContext();

    @Test
    @DisplayName("Проверка работы accept + записывания данных в context")
    void responseSizeTest() {
        context.setTotalRequestsCount(EXAMPLES_LOG.size()); // перед ResponseSizeStats должен вызываться ResponseStats, который устанавливает количество запросов в контекс

        EXAMPLES_LOG.forEach(analyzer::accept);
        analyzer.applyToContext(context);

        ResponseSize responseSizeInContext = context.getResponseSizeInBytes();
        assertEquals(321.2, responseSizeInContext.average(),
            "Значение average не соответствует действительности");
        assertEquals(500, responseSizeInContext.max(),
            "Значение max не соответствует действительности");
        assertEquals(500, responseSizeInContext.p95(),
            "Значение p95 не соответствует действительности");
    }
}
