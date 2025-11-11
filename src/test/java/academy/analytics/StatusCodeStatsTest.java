package academy.analytics;

import academy.util.AnalysisContext;
import academy.util.ParsedLog;
import academy.util.ResponseCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static academy.analytics.TestConstants.EXAMPLES_LOG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StatusCodeStatsTest {
    private final AnalyzerModule analyzer = new StatusCodeStats();
    private final AnalysisContext context = new AnalysisContext();

    @Test
    @DisplayName("Проверка работы accept + записывания данных в context")
    void statusCodeTest() {
        EXAMPLES_LOG.forEach(analyzer::accept);
        analyzer.applyToContext(context);

        List<ResponseCode> responseCodes = context.getResponseCodes();

        assertEquals(4, responseCodes.size());

        assertTrue(responseCodes.stream().anyMatch(rc -> rc.code() == 200 && rc.totalResponsesCount() == 2));
        assertTrue(responseCodes.stream().anyMatch(rc -> rc.code() == 404 && rc.totalResponsesCount() == 1));
        assertTrue(responseCodes.stream().anyMatch(rc -> rc.code() == 302 && rc.totalResponsesCount() == 1));
        assertTrue(responseCodes.stream().anyMatch(rc -> rc.code() == 500 && rc.totalResponsesCount() == 1));

        int totalCount = responseCodes.stream().mapToInt(ResponseCode::totalResponsesCount).sum();
        assertEquals(5, totalCount);
    }
}
