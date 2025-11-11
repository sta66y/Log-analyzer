package academy.analytics;

import academy.util.ParsedLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class AnalyzerTest {
    private static List<AnalyzerModule> mockedModules;
    private Analyzer analyzer;

    @BeforeEach
    void setUp() {
        mockedModules = List.of(
            mock(RequestStats.class),
            mock(ResponseSizeStats.class),
            mock(StatusCodeStats.class),
            mock(TopResourcesStats.class),
            mock(DateDistributionStats.class),
            mock(ProtocolStats.class)
        );
    }

    @Test
    @DisplayName("Проверяем, что все модули анализа вызываются")
    void analyseLog_ShouldUseAllModules() {
        analyzer = new Analyzer(null, null, mockedModules);

        ParsedLog testLog = new ParsedLog("10.0.0.55", "webapp-user", "admin", ZonedDateTime.parse("2023-10-15T14:50:15Z"), "PUT", "/api/users/123", "HTTP/2.0", 500, 0, "https://admin.mysite.com/users", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/118.0");

        analyzer.analyseLog(Stream.of(testLog));

        for (AnalyzerModule mockedModule : mockedModules) {
            verify(mockedModule).accept(testLog);
        }
    }

    @Test
    @DisplayName("Фильтрация: логи ДО dateFrom должны отфильтровываться")
    void isWithinDateRange_ShouldFilterLogsBeforeDateFrom() {
        // все логи из EXAMPLES_LOG до этой даты
        ZonedDateTime dateFrom = ZonedDateTime.parse("2023-10-16T00:00:00Z");
        analyzer = new Analyzer(dateFrom, null, mockedModules);

        analyzer.analyseLog(TestConstants.EXAMPLES_LOG.stream());

        // только логи с 2024 годом должны пройти фильтрацию
        ParsedLog log2024 = TestConstants.EXAMPLES_LOG.get(0); // 2024-10-15
        for (AnalyzerModule mockedModule : mockedModules) {
            verify(mockedModule).accept(log2024);
            // логи 2023 года не должны быть обработаны
            for (int i = 1; i < TestConstants.EXAMPLES_LOG.size(); i++) {
                verify(mockedModule, never()).accept(TestConstants.EXAMPLES_LOG.get(i));
            }
        }
    }

    @Test
    @DisplayName("Фильтрация: логи ПОСЛЕ dateTo должны отфильтровываться")
    void isWithinDateRange_ShouldFilterLogsAfterDateTo() {
        // все логи из EXAMPLES_LOG после этой даты
        ZonedDateTime dateTo = ZonedDateTime.parse("2023-10-14T23:59:59Z");
        analyzer = new Analyzer(null, dateTo, mockedModules);

        analyzer.analyseLog(TestConstants.EXAMPLES_LOG.stream());

        // все логи должны быть отфильтрованы
        for (AnalyzerModule mockedModule : mockedModules) {
            for (ParsedLog log : TestConstants.EXAMPLES_LOG) {
                verify(mockedModule, never()).accept(log);
            }
        }
    }

    @Test
    @DisplayName("Фильтрация: логи в диапазоне dateFrom-dateTo должны обрабатываться")
    void isWithinDateRange_ShouldProcessLogsWithinRange() {
        ZonedDateTime dateFrom = ZonedDateTime.parse("2023-10-14T00:00:00Z");
        ZonedDateTime dateTo = ZonedDateTime.parse("2023-10-16T23:59:59Z");
        analyzer = new Analyzer(dateFrom, dateTo, mockedModules);

        analyzer.analyseLog(TestConstants.EXAMPLES_LOG.stream());

        // только логи 2023 года должны быть обработаны
        for (AnalyzerModule mockedModule : mockedModules) {
            for (int i = 1; i < TestConstants.EXAMPLES_LOG.size(); i++) {
                verify(mockedModule).accept(TestConstants.EXAMPLES_LOG.get(i));
            }
            verify(mockedModule, never()).accept(TestConstants.EXAMPLES_LOG.get(0));
        }
    }

    @Test
    @DisplayName("Фильтрация: без ограничений дат должны обрабатываться все логи")
    void isWithinDateRange_ShouldProcessAllLogsWhenNoDateRange() {
        // нет ограничений по дате
        analyzer = new Analyzer(null, null, mockedModules);

        analyzer.analyseLog(TestConstants.EXAMPLES_LOG.stream());

        for (AnalyzerModule mockedModule : mockedModules) {
            for (ParsedLog log : TestConstants.EXAMPLES_LOG) {
                verify(mockedModule).accept(log);
            }
        }
    }

    @Test
    @DisplayName("Фильтрация: граничные значения должны включаться")
    void isWithinDateRange_ShouldIncludeBoundaryDates() {
        // границы точно совпадают с датами логов
        ZonedDateTime dateFrom = ZonedDateTime.parse("2023-10-15T14:35:10Z"); // точное время второго лога
        ZonedDateTime dateTo = ZonedDateTime.parse("2023-10-15T14:50:15Z");   // точное время последнего лога
        analyzer = new Analyzer(dateFrom, dateTo, mockedModules);

        analyzer.analyseLog(TestConstants.EXAMPLES_LOG.stream());

        // логи с граничными датами должны быть обработаны
        for (AnalyzerModule mockedModule : mockedModules) {
            for (int i = 1; i < TestConstants.EXAMPLES_LOG.size(); i++) {
                verify(mockedModule).accept(TestConstants.EXAMPLES_LOG.get(i));
            }
            verify(mockedModule, never()).accept(TestConstants.EXAMPLES_LOG.get(0));
        }
    }

    @Test
    @DisplayName("Фильтрация: пустой stream не должен вызывать исключений")
    void isWithinDateRange_ShouldHandleEmptyStream() {
        analyzer = new Analyzer(null, null, mockedModules);

        // пустой stream
        analyzer.analyseLog(Stream.empty());

        for (AnalyzerModule mockedModule : mockedModules) {
            verify(mockedModule, never()).accept(org.mockito.ArgumentMatchers.any());
        }
    }
}
