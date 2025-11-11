package academy.analytics;

import academy.util.AnalysisContext;
import academy.util.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static academy.analytics.TestConstants.EXAMPLES_LOG;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TopResourcesStatsTest {
    private final AnalyzerModule analyzer = new TopResourcesStats();
    private final AnalysisContext context = new AnalysisContext();

    @Test
    @DisplayName("Проверка работы accept + записывания данных в context")
    void statusCodeTest() {
        EXAMPLES_LOG.forEach(analyzer::accept);
        analyzer.applyToContext(context);

        List<Resource> resources = context.getResources();

        assertEquals(4, resources.size()); // в тестовых данных 5 логов, 2 из которых имеют одинаковое поле resource

        assertEquals("/index.html", resources.getFirst().resource());
        assertEquals(2, resources.getFirst().totalRequestsCount());
    }
}
