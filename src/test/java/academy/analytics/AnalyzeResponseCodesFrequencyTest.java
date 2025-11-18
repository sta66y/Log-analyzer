package academy.analytics;

import static academy.analytics.TestConstants.EXAMPLES_LOG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import academy.model.AnalysisContext;
import academy.model.ResponseCode;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AnalyzeResponseCodesFrequencyTest {
    private final AnalyzerModule analyzer = new AnalyzeResponseCodesFrequency();
    private final AnalysisContext context = new AnalysisContext();

    @Test
    @DisplayName("Проверка работы accept + записывание данных в context")
    void statusCodeTest() {
        EXAMPLES_LOG.forEach(analyzer::accept);
        analyzer.applyToContext(context);

        List<ResponseCode> responseCodes = context.getResponseCodes();

        assertEquals(4, responseCodes.size());

        assertTrue(responseCodes.stream().anyMatch(rc -> rc.code() == 200 && rc.totalResponsesCount() == 2));
        assertTrue(responseCodes.stream().anyMatch(rc -> rc.code() == 404 && rc.totalResponsesCount() == 1));
        assertTrue(responseCodes.stream().anyMatch(rc -> rc.code() == 302 && rc.totalResponsesCount() == 1));
        assertTrue(responseCodes.stream().anyMatch(rc -> rc.code() == 500 && rc.totalResponsesCount() == 1));

        int totalCount = responseCodes.stream()
                .mapToInt(ResponseCode::totalResponsesCount)
                .sum();
        assertEquals(5, totalCount);
    }
}
