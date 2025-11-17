package academy.analytics;

import academy.model.AnalysisContext;
import academy.model.Date;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static academy.analytics.TestConstants.EXAMPLES_LOG;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AnalyzeDateDistributionTest {
    private final AnalyzerModule analyzer = new AnalyzeDateDistribution();
    private final AnalysisContext context = new AnalysisContext();

    @Test
    @DisplayName("Проверка работы accept + записывание данных в context")
    void statusCodeTest() {
        context.setTotalRequestsCount(EXAMPLES_LOG.size());

        EXAMPLES_LOG.forEach(analyzer::accept);
        analyzer.applyToContext(context);

        List<Date> requestsPerDate = context.getRequestsPerDate();

        // должно быть 2 уникальных дня: 2023-10-15 (4 запроса) и 2024-10-15 (1 запрос)
        assertEquals(2, requestsPerDate.size());

        // данные для 2023-10-15
        Date date2023 = requestsPerDate.stream()
            .filter(d -> d.date().equals("2023-10-15"))
            .findFirst()
            .orElseThrow();
        assertEquals("Sunday", date2023.weekday()); // 15 октября 2023 - воскресенье
        assertEquals(4, date2023.totalRequestsCount());
        assertEquals(80.0, date2023.totalRequestsPercentage()); // 4/5 = 0.8 * 100

        // данные для 2024-10-15
        Date date2024 = requestsPerDate.stream()
            .filter(d -> d.date().equals("2024-10-15"))
            .findFirst()
            .orElseThrow();
        assertEquals("Tuesday", date2024.weekday()); // 15 октября 2024 - вторник
        assertEquals(1, date2024.totalRequestsCount());
        assertEquals(20.0, date2024.totalRequestsPercentage()); // 1/5 = 0.2 * 100

        // проверяем общую сумму запросов
        int totalRequests = requestsPerDate.stream()
            .mapToInt(Date::totalRequestsCount)
            .sum();
        assertEquals(5, totalRequests);

        double totalPercentage = requestsPerDate.stream()
            .mapToDouble(Date::totalRequestsPercentage)
            .sum();
        assertEquals(100.0, totalPercentage, 0.001);
    }
}
