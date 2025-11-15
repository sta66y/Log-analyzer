package academy.analytics;

import academy.model.AnalysisContext;
import academy.model.ParsedLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnalyzeTotalCountRequestsTest {
    private AnalyzerModule analyzer;
    private AnalysisContext context;

    @BeforeEach
    void createContext() {
        context = new AnalysisContext();
        analyzer = new AnalyzeTotalCountRequests();
    }

    @Test
    @DisplayName("Проверка изменения AnalysisContext")
    void applyToContext_ShouldChangeContext() {
        context.setTotalRequestsCount(999);

        analyzer.applyToContext(context);

        assertEquals(0, context.getTotalRequestsCount(),
            "Значение TotalRequestsCount в context не поменялось"); // при инициализации cntRequests в RequestStats = 0
    }

    @Test
    @DisplayName("Проверка работы incrementCount")
    void acceptTest() {
        context.setTotalRequestsCount(0);
        ParsedLog log = null; // не суть важно

        analyzer.accept(log);
        analyzer.applyToContext(context);

        assertEquals(1, context.getTotalRequestsCount(),
            "Значение TotalRequestsCount в context не поменялось");
    }

}
