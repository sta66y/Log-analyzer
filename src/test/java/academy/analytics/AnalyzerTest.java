package academy.analytics;

import academy.io.input.Reader;
import academy.parser.LogsParser;
import academy.model.AnalysisContext;
import academy.model.ParsedLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AnalyzerTest {

    @Mock
    private Reader reader1;

    @Mock
    private Reader reader2;

    @Mock
    private LogsParser logsParser;

    private List<AnalyzerModule> mockedModules;
    private Analyzer analyzer;

    @BeforeEach
    void setUp() {
        mockedModules = List.of(
            mock(AnalyzeTotalCountRequests.class),
            mock(AnalyzeResponseSize.class),
            mock(AnalyzeResponseCodesFrequency.class),
            mock(AnalyzeTopMostFrequentResources.class),
            mock(AnalyzeDateDistribution.class),
            mock(AnalyzeUniqueProtocols.class)
        );
    }

    @Test
    @DisplayName("Должен использовать все модули для каждого лога")
    void analyse_ShouldUseAllModulesForEachLog() throws IOException {
        when(reader1.getPath()).thenReturn("/path/to/file1.log");
        when(reader2.getPath()).thenReturn("/path/to/file2.log");

        List<Reader> readers = List.of(reader1, reader2);

        ParsedLog log1 = new ParsedLog("10.0.0.1", "user1", "user1",
            ZonedDateTime.parse("2023-10-15T10:00:00Z"), "GET", "/api/test", "HTTP/1.1", 200, 100, "http://test.com", "Mozilla");
        ParsedLog log2 = new ParsedLog("10.0.0.2", "user2", "user2",
            ZonedDateTime.parse("2023-10-15T11:00:00Z"), "POST", "/api/test", "HTTP/1.1", 201, 150, "http://test.com", "Mozilla");
        ParsedLog log3 = new ParsedLog("10.0.0.3", "user3", "user3",
            ZonedDateTime.parse("2023-10-15T12:00:00Z"), "DELETE", "/api/test", "HTTP/1.1", 204, 0, "http://test.com", "Mozilla");

        Stream<ParsedLog> parsedLogs = Stream.of(log1, log2, log3);
        when(logsParser.parseAndFilterLogs(eq(readers), isNull(), isNull())).thenReturn(parsedLogs);

        analyzer = new Analyzer(mockedModules, logsParser);

        AnalysisContext result = analyzer.analyse(readers, null, null);

        for (AnalyzerModule module : mockedModules) {
            verify(module).accept(log1);
            verify(module).accept(log2);
            verify(module).accept(log3);
            verify(module).applyToContext(any(AnalysisContext.class));
        }

        assertEquals(List.of("/path/to/file1.log", "/path/to/file2.log"), result.getFiles());
        assertNull(result.getDateFrom());
        assertNull(result.getDateTo());
    }

    @Test
    @DisplayName("Фильтрация: логи ДО dateFrom должны отфильтровываться")
    void analyse_ShouldFilterLogsBeforeDateFrom() throws IOException {
        when(reader1.getPath()).thenReturn("/path/to/file.log");
        List<Reader> readers = List.of(reader1);

        ParsedLog logAfter = new ParsedLog("10.0.0.2", "user2", "user2",
            ZonedDateTime.parse("2023-10-15T00:00:00Z"), "POST", "/api/test", "HTTP/1.1", 201, 150, "http://test.com", "Mozilla");

        Stream<ParsedLog> filteredLogs = Stream.of(logAfter);
        when(logsParser.parseAndFilterLogs(eq(readers), any(LocalDate.class), isNull()))
            .thenReturn(filteredLogs);

        analyzer = new Analyzer(mockedModules, logsParser);
        LocalDate dateFrom = LocalDate.parse("2023-10-15");

        analyzer.analyse(readers, dateFrom, null);

        for (AnalyzerModule module : mockedModules) {
            verify(module).accept(logAfter);
            verify(module).applyToContext(any(AnalysisContext.class));
        }
    }

    @Test
    @DisplayName("Фильтрация: логи ПОСЛЕ dateTo должны отфильтровываться")
    void analyse_ShouldFilterLogsAfterDateTo() throws IOException {
        when(reader1.getPath()).thenReturn("/path/to/file.log");
        List<Reader> readers = List.of(reader1);

        ParsedLog logBefore = new ParsedLog("10.0.0.1", "user1", "user1",
            ZonedDateTime.parse("2023-10-14T23:59:59Z"), "GET", "/api/test", "HTTP/1.1", 200, 100, "http://test.com", "Mozilla");

        Stream<ParsedLog> filteredLogs = Stream.of(logBefore);
        when(logsParser.parseAndFilterLogs(eq(readers), isNull(), any(LocalDate.class)))
            .thenReturn(filteredLogs);

        analyzer = new Analyzer(mockedModules, logsParser);
        LocalDate dateTo = LocalDate.parse("2023-10-14");

        analyzer.analyse(readers, null, dateTo);

        for (AnalyzerModule module : mockedModules) {
            verify(module).accept(logBefore);
            verify(module).applyToContext(any(AnalysisContext.class));
        }
    }

    @Test
    @DisplayName("Фильтрация: логи в диапазоне dateFrom-dateTo должны обрабатываться")
    void analyse_ShouldAnalyzeLogsWithinRange() throws IOException {
        when(reader1.getPath()).thenReturn("/path/to/file.log");
        List<Reader> readers = List.of(reader1);

        ParsedLog log2 = new ParsedLog("10.0.0.2", "user2", "user2",
            ZonedDateTime.parse("2023-10-15T10:00:00Z"), "POST", "/api/test", "HTTP/1.1", 201, 150, "http://test.com", "Mozilla");

        Stream<ParsedLog> filteredLogs = Stream.of(log2);
        when(logsParser.parseAndFilterLogs(eq(readers), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(filteredLogs);

        analyzer = new Analyzer(mockedModules, logsParser);
        LocalDate dateFrom = LocalDate.parse("2023-10-15");
        LocalDate dateTo = LocalDate.parse("2023-10-15");

        analyzer.analyse(readers, dateFrom, dateTo);

        for (AnalyzerModule module : mockedModules) {
            verify(module).accept(log2);
            verify(module).applyToContext(any(AnalysisContext.class));
        }
    }

    @Test
    @DisplayName("Без ограничений дат должны обрабатываться все логи")
    void analyse_ShouldProcessAllLogsWhenNoDateRange() throws IOException {
        when(reader1.getPath()).thenReturn("/path/to/file.log");
        List<Reader> readers = List.of(reader1);

        ParsedLog log1 = new ParsedLog("10.0.0.1", "user1", "user1",
            ZonedDateTime.parse("2023-10-14T10:00:00Z"), "GET", "/api/test", "HTTP/1.1", 200, 100, "http://test.com", "Mozilla");
        ParsedLog log2 = new ParsedLog("10.0.0.2", "user2", "user2",
            ZonedDateTime.parse("2023-10-15T10:00:00Z"), "POST", "/api/test", "HTTP/1.1", 201, 150, "http://test.com", "Mozilla");

        Stream<ParsedLog> allLogs = Stream.of(log1, log2);
        when(logsParser.parseAndFilterLogs(eq(readers), isNull(), isNull())).thenReturn(allLogs);

        analyzer = new Analyzer(mockedModules, logsParser);

        analyzer.analyse(readers, null, null);

        for (AnalyzerModule module : mockedModules) {
            verify(module).accept(log1);
            verify(module).accept(log2);
            verify(module).applyToContext(any(AnalysisContext.class));
        }
    }

    @Test
    @DisplayName("Пустой stream не должен вызывать исключений")
    void analyse_ShouldHandleEmptyStream() throws IOException {
        when(reader1.getPath()).thenReturn("/path/to/file.log");
        List<Reader> readers = List.of(reader1);

        Stream<ParsedLog> emptyStream = Stream.empty();
        when(logsParser.parseAndFilterLogs(eq(readers), isNull(), isNull())).thenReturn(emptyStream);

        analyzer = new Analyzer(mockedModules, logsParser);

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
    void analyse_ShouldIncludeBoundaryDates() throws IOException {
        when(reader1.getPath()).thenReturn("/path/to/file.log");
        List<Reader> readers = List.of(reader1);

        ParsedLog boundaryLog = new ParsedLog("10.0.0.1", "user1", "user1",
            ZonedDateTime.parse("2023-10-15T00:00:00Z"), "GET", "/api/test", "HTTP/1.1", 200, 100, "http://test.com", "Mozilla");

        Stream<ParsedLog> boundaryLogs = Stream.of(boundaryLog);
        when(logsParser.parseAndFilterLogs(eq(readers), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(boundaryLogs);

        analyzer = new Analyzer(mockedModules, logsParser);
        LocalDate dateFrom = LocalDate.parse("2023-10-15");
        LocalDate dateTo = LocalDate.parse("2023-10-15");

        analyzer.analyse(readers, dateFrom, dateTo);

        for (AnalyzerModule module : mockedModules) {
            verify(module).accept(boundaryLog);
            verify(module).applyToContext(any(AnalysisContext.class));
        }
    }
}
