package academy.parser;

import academy.io.input.Reader;
import academy.model.ParsedLog;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public record LogsParser(PathParser pathParser) {

    public Stream<ParsedLog> parseAndFilterLogs(List<Reader> readers, LocalDate dateFrom, LocalDate dateTo) throws IOException {
        List<String> allLines = readers.stream()
            .flatMap(Reader::read)
            .toList();

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
