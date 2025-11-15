package academy.analytics;

import academy.io.input.Reader;
import academy.parser.Parser;
import academy.util.AnalysisContext;
import academy.util.ParsedLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnalyzerTest {

    @Mock
    private Reader reader1;

    @Mock
    private Reader reader2;

    @Mock
    private Parser parser;

    private List<AnalyzerModule> mockedModules;
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
    @DisplayName("Должен использовать все модули для каждого лога")
    void analyse_ShouldUseAllModulesForEachLog() {
        when(reader1.getPath()).thenReturn("/path/to/file1.log");
        when(reader2.getPath()).thenReturn("/path/to/file2.log");

        List<Reader> readers = List.of(reader1, reader2);

        when(reader1.read()).thenReturn(Stream.of("line1", "line2"));
        when(reader2.read()).thenReturn(Stream.of("line3"));

        ParsedLog log1 = new ParsedLog("10.0.0.1", "user1", "user1",
            ZonedDateTime.parse("2023-10-15T10:00:00Z"), "GET", "/api/test", "HTTP/1.1", 200, 100, "http://test.com", "Mozilla");
        ParsedLog log2 = new ParsedLog("10.0.0.2", "user2", "user2",
            ZonedDateTime.parse("2023-10-15T11:00:00Z"), "POST", "/api/test", "HTTP/1.1", 201, 150, "http://test.com", "Mozilla");
        ParsedLog log3 = new ParsedLog("10.0.0.3", "user3", "user3",
            ZonedDateTime.parse("2023-10-15T12:00:00Z"), "DELETE", "/api/test", "HTTP/1.1", 204, 0, "http://test.com", "Mozilla");

        when(parser.parseLine("line1")).thenReturn(log1);
        when(parser.parseLine("line2")).thenReturn(log2);
        when(parser.parseLine("line3")).thenReturn(log3);

        analyzer = new Analyzer(mockedModules, parser);

        AnalysisContext result = analyzer.analyse(readers, null, null);

        for (AnalyzerModule module : mockedModules) {
            verify(module).accept(log1);
            verify(module).accept(log2);
            verify(module).accept(log3);
            verify(module).applyToContext(any(AnalysisContext.class));
        }

        assertEquals(List.of("/path/to/file1.log", "/path/to/file2.log"), result.getFiles());
        assertNull(result.getStartDate());
        assertNull(result.getEndDate());
    }

    @Test
    @DisplayName("Фильтрация: логи ДО dateFrom должны отфильтровываться")
    void analyse_ShouldFilterLogsBeforeDateFrom() {
        when(reader1.getPath()).thenReturn("/path/to/file.log");

        List<Reader> readers = List.of(reader1);

        ParsedLog logBefore = new ParsedLog("10.0.0.1", "user1", "user1",
            ZonedDateTime.parse("2023-10-14T23:59:59Z"), "GET", "/api/test", "HTTP/1.1", 200, 100, "http://test.com", "Mozilla");
        ParsedLog logAfter = new ParsedLog("10.0.0.2", "user2", "user2",
            ZonedDateTime.parse("2023-10-15T00:00:00Z"), "POST", "/api/test", "HTTP/1.1", 201, 150, "http://test.com", "Mozilla");

        when(reader1.read()).thenReturn(Stream.of("line1", "line2"));
        when(parser.parseLine("line1")).thenReturn(logBefore);
        when(parser.parseLine("line2")).thenReturn(logAfter);

        analyzer = new Analyzer(mockedModules, parser);
        LocalDate dateFrom = LocalDate.parse("2023-10-15");

        analyzer.analyse(readers, dateFrom, null);

        for (AnalyzerModule module : mockedModules) {
            verify(module, never()).accept(logBefore); // Должен быть отфильтрован
            verify(module).accept(logAfter); // Должен пройти
            verify(module).applyToContext(any(AnalysisContext.class));
        }
    }

    @Test
    @DisplayName("Фильтрация: логи ПОСЛЕ dateTo должны отфильтровываться")
    void analyse_ShouldFilterLogsAfterDateTo() {
        when(reader1.getPath()).thenReturn("/path/to/file.log");

        List<Reader> readers = List.of(reader1);

        ParsedLog logBefore = new ParsedLog("10.0.0.1", "user1", "user1",
            ZonedDateTime.parse("2023-10-14T23:59:59Z"), "GET", "/api/test", "HTTP/1.1", 200, 100, "http://test.com", "Mozilla");
        ParsedLog logAfter = new ParsedLog("10.0.0.2", "user2", "user2",
            ZonedDateTime.parse("2023-10-15T00:00:00Z"), "POST", "/api/test", "HTTP/1.1", 201, 150, "http://test.com", "Mozilla");

        when(reader1.read()).thenReturn(Stream.of("line1", "line2"));
        when(parser.parseLine("line1")).thenReturn(logBefore);
        when(parser.parseLine("line2")).thenReturn(logAfter);

        analyzer = new Analyzer(mockedModules, parser);
        LocalDate dateTo = LocalDate.parse("2023-10-14");

        analyzer.analyse(readers, null, dateTo);

        for (AnalyzerModule module : mockedModules) {
            verify(module).accept(logBefore); // Должен пройти
            verify(module, never()).accept(logAfter); // Должен быть отфильтрован
            verify(module).applyToContext(any(AnalysisContext.class));
        }
    }

    @Test
    @DisplayName("Фильтрация: логи в диапазоне dateFrom-dateTo должны обрабатываться")
    void analyse_ShouldProcessLogsWithinRange() {
        when(reader1.getPath()).thenReturn("/path/to/file.log");

        List<Reader> readers = List.of(reader1);

        ParsedLog log1 = new ParsedLog("10.0.0.1", "user1", "user1",
            ZonedDateTime.parse("2023-10-14T10:00:00Z"), "GET", "/api/test", "HTTP/1.1", 200, 100, "http://test.com", "Mozilla");
        ParsedLog log2 = new ParsedLog("10.0.0.2", "user2", "user2",
            ZonedDateTime.parse("2023-10-15T10:00:00Z"), "POST", "/api/test", "HTTP/1.1", 201, 150, "http://test.com", "Mozilla");
        ParsedLog log3 = new ParsedLog("10.0.0.3", "user3", "user3",
            ZonedDateTime.parse("2023-10-16T10:00:00Z"), "DELETE", "/api/test", "HTTP/1.1", 204, 0, "http://test.com", "Mozilla");

        when(reader1.read()).thenReturn(Stream.of("line1", "line2", "line3"));
        when(parser.parseLine("line1")).thenReturn(log1);
        when(parser.parseLine("line2")).thenReturn(log2);
        when(parser.parseLine("line3")).thenReturn(log3);

        analyzer = new Analyzer(mockedModules, parser);
        LocalDate dateFrom = LocalDate.parse("2023-10-15");
        LocalDate dateTo = LocalDate.parse("2023-10-15");

        analyzer.analyse(readers, dateFrom, dateTo);

        for (AnalyzerModule module : mockedModules) {
            verify(module, never()).accept(log1); // До диапазона
            verify(module).accept(log2); // В диапазоне
            verify(module, never()).accept(log3); // После диапазона
            verify(module).applyToContext(any(AnalysisContext.class));
        }
    }

    @Test
    @DisplayName("Без ограничений дат должны обрабатываться все логи")
    void analyse_ShouldProcessAllLogsWhenNoDateRange() {
        when(reader1.getPath()).thenReturn("/path/to/file.log");

        List<Reader> readers = List.of(reader1);

        ParsedLog log1 = new ParsedLog("10.0.0.1", "user1", "user1",
            ZonedDateTime.parse("2023-10-14T10:00:00Z"), "GET", "/api/test", "HTTP/1.1", 200, 100, "http://test.com", "Mozilla");
        ParsedLog log2 = new ParsedLog("10.0.0.2", "user2", "user2",
            ZonedDateTime.parse("2023-10-15T10:00:00Z"), "POST", "/api/test", "HTTP/1.1", 201, 150, "http://test.com", "Mozilla");

        when(reader1.read()).thenReturn(Stream.of("line1", "line2"));
        when(parser.parseLine("line1")).thenReturn(log1);
        when(parser.parseLine("line2")).thenReturn(log2);

        analyzer = new Analyzer(mockedModules, parser);

        analyzer.analyse(readers, null, null);

        for (AnalyzerModule module : mockedModules) {
            verify(module).accept(log1);
            verify(module).accept(log2);
            verify(module).applyToContext(any(AnalysisContext.class));
        }
    }

    @Test
    @DisplayName("Пустой stream не должен вызывать исключений")
    void analyse_ShouldHandleEmptyStream() {
        when(reader1.getPath()).thenReturn("/path/to/file.log");

        List<Reader> readers = List.of(reader1);
        when(reader1.read()).thenReturn(Stream.empty());

        analyzer = new Analyzer(mockedModules, parser);

        AnalysisContext result = analyzer.analyse(readers, null, null);

        for (AnalyzerModule module : mockedModules) {
            verify(module, never()).accept(any(ParsedLog.class));
            verify(module).applyToContext(any(AnalysisContext.class));
        }

        assertNotNull(result);
        assertEquals(List.of("/path/to/file.log"), result.getFiles());
    }

    @Test
    @DisplayName("Граничные значения должны включаться")
    void analyse_ShouldIncludeBoundaryDates() {
        when(reader1.getPath()).thenReturn("/path/to/file.log");

        List<Reader> readers = List.of(reader1);

        ParsedLog boundaryLog = new ParsedLog("10.0.0.1", "user1", "user1",
            ZonedDateTime.parse("2023-10-15T00:00:00Z"), "GET", "/api/test", "HTTP/1.1", 200, 100, "http://test.com", "Mozilla");

        when(reader1.read()).thenReturn(Stream.of("line1"));
        when(parser.parseLine("line1")).thenReturn(boundaryLog);

        analyzer = new Analyzer(mockedModules, parser);
        LocalDate dateFrom = LocalDate.parse("2023-10-15");
        LocalDate dateTo = LocalDate.parse("2023-10-15");

        analyzer.analyse(readers, dateFrom, dateTo);

        for (AnalyzerModule module : mockedModules) {
            verify(module).accept(boundaryLog); // Граничное значение должно пройти
            verify(module).applyToContext(any(AnalysisContext.class));
        }
    }

}
