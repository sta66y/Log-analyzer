package academy.analytics;

import academy.util.AnalysisContext;
import academy.util.ParsedLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
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
    private AnalysisContext context;

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

        context = new AnalysisContext();
    }

    @Test
    @DisplayName("Проверяем, что все модули анализа вызываются")
    void analyseLog_ShouldUseAllModules() {
        analyzer = new Analyzer(context, null, null, mockedModules);

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
        LocalDate dateFrom = LocalDate.parse("2023-10-16");
        analyzer = new Analyzer(context, dateFrom, null, mockedModules);

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
        LocalDate dateTo = LocalDate.parse("2023-10-14");
        analyzer = new Analyzer(context, null, dateTo, mockedModules);

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
        LocalDate dateFrom = LocalDate.parse("2023-10-14");
        LocalDate dateTo = LocalDate.parse("2023-10-16");
        analyzer = new Analyzer(context, dateFrom, dateTo, mockedModules);

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
        analyzer = new Analyzer(context, null, null, mockedModules);

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
        // границы точно совпадают с датами логов (преобразованные в LocalDate)
        LocalDate dateFrom = LocalDate.parse("2023-10-15"); // дата второго лога
        LocalDate dateTo = LocalDate.parse("2023-10-15");   // дата последнего лога
        analyzer = new Analyzer(context, dateFrom, dateTo, mockedModules);

        analyzer.analyseLog(TestConstants.EXAMPLES_LOG.stream());

        // логи с граничными датами должны быть обработаны
        for (AnalyzerModule mockedModule : mockedModules) {
            // Обрабатываем только логи с датой 2023-10-15
            for (int i = 1; i < TestConstants.EXAMPLES_LOG.size(); i++) {
                ParsedLog log = TestConstants.EXAMPLES_LOG.get(i);
                if (log.date().toLocalDate().equals(LocalDate.parse("2023-10-15"))) {
                    verify(mockedModule).accept(log);
                } else {
                    verify(mockedModule, never()).accept(log);
                }
            }
            verify(mockedModule, never()).accept(TestConstants.EXAMPLES_LOG.get(0));
        }
    }

    @Test
    @DisplayName("Фильтрация: пустой stream не должен вызывать исключений")
    void isWithinDateRange_ShouldHandleEmptyStream() {
        analyzer = new Analyzer(context, null, null, mockedModules);

        // пустой stream
        analyzer.analyseLog(Stream.empty());

        for (AnalyzerModule mockedModule : mockedModules) {
            verify(mockedModule, never()).accept(org.mockito.ArgumentMatchers.any());
        }
    }

    @Test
    @DisplayName("Фильтрация: логи с той же датой что и dateFrom/dateTo должны включаться")
    void isWithinDateRange_ShouldIncludeSameDates() {
        LocalDate dateFrom = LocalDate.parse("2023-10-15");
        LocalDate dateTo = LocalDate.parse("2023-10-15");
        analyzer = new Analyzer(context, dateFrom, dateTo, mockedModules);

        // Создаем логи с разным временем но одинаковой датой
        ParsedLog morningLog = new ParsedLog("10.0.0.1", "user1", "user1",
            ZonedDateTime.parse("2023-10-15T08:00:00Z"), "GET", "/api/test", "HTTP/1.1", 200, 100, "http://test.com", "Mozilla");
        ParsedLog noonLog = new ParsedLog("10.0.0.2", "user2", "user2",
            ZonedDateTime.parse("2023-10-15T12:00:00Z"), "POST", "/api/test", "HTTP/1.1", 201, 150, "http://test.com", "Mozilla");
        ParsedLog eveningLog = new ParsedLog("10.0.0.3", "user3", "user3",
            ZonedDateTime.parse("2023-10-15T18:00:00Z"), "DELETE", "/api/test", "HTTP/1.1", 204, 0, "http://test.com", "Mozilla");

        analyzer.analyseLog(Stream.of(morningLog, noonLog, eveningLog));

        // Все логи с датой 2023-10-15 должны быть обработаны
        for (AnalyzerModule mockedModule : mockedModules) {
            verify(mockedModule).accept(morningLog);
            verify(mockedModule).accept(noonLog);
            verify(mockedModule).accept(eveningLog);
        }
    }
}
