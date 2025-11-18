package academy.analytics;

import static academy.analytics.TestConstants.EXAMPLES_LOG;
import static org.junit.jupiter.api.Assertions.assertEquals;

import academy.model.AnalysisContext;
import academy.model.Resource;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AnalyzeTopMostFrequentResourcesTest {
    private final AnalyzerModule analyzer = new AnalyzeTopMostFrequentResources();
    private final AnalysisContext context = new AnalysisContext();

    @Test
    @DisplayName("Проверка работы accept + записывание данных в context")
    void statusCodeTest() {
        EXAMPLES_LOG.forEach(analyzer::accept);
        analyzer.applyToContext(context);

        List<Resource> resources = context.getResources();

        assertEquals(4, resources.size()); // в тестовых данных 5 логов, 2 из которых имеют одинаковое поле resource

        assertEquals("/index.html", resources.getFirst().resource());
        assertEquals(2, resources.getFirst().totalRequestsCount());
    }
}
