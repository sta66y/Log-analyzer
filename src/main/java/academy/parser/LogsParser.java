package academy.parser;

import academy.io.input.Reader;
import academy.model.ParsedLog;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**  Парсит и фильтрует логи по дате. */
public class LogsParser {
    private final PathParser pathParser;

    public LogsParser(PathParser pathParser) {
        this.pathParser = pathParser;
    }
    /**
     * Парсит и фильтрует логи по диапазону дат.
     *
     * @param readers список источников логов
     * @param dateFrom начальная дата (включительно)
     * @param dateTo конечная дата (включительно)
     * @return поток распарсенных логов в указанном диапазоне дат
     * @throws IOException если произошла ошибка чтения
     */
    public Stream<ParsedLog> parseAndFilterLogs(List<Reader> readers, LocalDate dateFrom, LocalDate dateTo) throws IOException {
        List<String> allLines = new ArrayList<>();

        for (Reader reader : readers) {
            try (Stream<String> lines = reader.read()) {
                allLines.addAll(lines.toList());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Чтение прервано: " + reader.getPath(), e);
            }
        }

        List<ParsedLog> parsedLogs = new ArrayList<>();
        for (String line : allLines) {
            ParsedLog parsedLog = pathParser.parseLine(line);
            parsedLogs.add(parsedLog);
        }

        return parsedLogs.stream()
            .filter(parsedLog -> isWithinDateRange(parsedLog, dateFrom, dateTo));
    }


    private boolean isWithinDateRange(ParsedLog log, LocalDate dateFrom, LocalDate dateTo) {
        LocalDate logDate = log.date().toLocalDate();
        return !((dateFrom != null && logDate.isBefore(dateFrom))
            || (dateTo != null && logDate.isAfter(dateTo)));
    }
}
