package academy.analytics;

import academy.model.AnalysisContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static academy.analytics.TestConstants.EXAMPLES_LOG;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProtocolStatsTest {
    private final AnalyzerModule analyzer = new ProtocolStats();
    private final AnalysisContext context = new AnalysisContext();

    @Test
    @DisplayName("Проверка работы accept + записывания данных в context")
    void statusCodeTest() {
        EXAMPLES_LOG.forEach(analyzer::accept);
        analyzer.applyToContext(context);

        Set<String> uniqueProtocols = context.getUniqueProtocols();

        // должно быть 2 протокола http1 и http2
        assertEquals(2, uniqueProtocols.size(), "Количество уникальных протоколов определено неверно");
    }
}
