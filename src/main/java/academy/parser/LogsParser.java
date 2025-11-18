package academy.parser;

import academy.io.input.Reader;
import academy.model.ParsedLog;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/** Парсит и фильтрует логи по дате. */
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
     * @throws RuntimeException если произошла ошибка чтения
     */
    public Stream<ParsedLog> parseAndFilterLogs(List<Reader> readers, LocalDate dateFrom, LocalDate dateTo) {
        return readers.stream()
                .flatMap(reader -> {
                    try {
                        return reader.read();
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(Objects::nonNull)
                .map(pathParser::parseLine)
                .filter(parsedLog -> isWithinDateRange(parsedLog, dateFrom, dateTo));
    }

    private boolean isWithinDateRange(ParsedLog log, LocalDate dateFrom, LocalDate dateTo) {
        if (log == null || log.date() == null) {
            return false;
        }

        LocalDate logDate = log.date().toLocalDate();
        return !((dateFrom != null && logDate.isBefore(dateFrom)) || (dateTo != null && logDate.isAfter(dateTo)));
    }
}
