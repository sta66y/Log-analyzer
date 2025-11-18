package academy.analytics;

import static academy.analytics.TestConstants.EXAMPLES_LOG;
import static org.junit.jupiter.api.Assertions.assertEquals;

import academy.model.AnalysisContext;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AnalyzeUniqueProtocolsTest {
    private final AnalyzerModule analyzer = new AnalyzeUniqueProtocols();
    private final AnalysisContext context = new AnalysisContext();

    @Test
    @DisplayName("Проверка работы accept + записывание данных в context")
    void statusCodeTest() {
        EXAMPLES_LOG.forEach(analyzer::accept);
        analyzer.applyToContext(context);

        Set<String> uniqueProtocols = context.getUniqueProtocols();

        // должно быть 2 протокола http1 и http2
        assertEquals(2, uniqueProtocols.size(), "Количество уникальных протоколов определено неверно");
    }
}
