package academy.parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Parser {
    private final Logger logger = LogManager.getLogger(Parser.class);

    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z");

    private static final Pattern PATTERN = Pattern.compile(
        "(?<id>\\S+)\\s" +
            "(?<clientId>\\S+)\\s" +
            "(?<userRFCId>\\S+)\\s" +
            "\\[(?<date>[^\\]]+)\\]\\s" +
            "\"(?<httpRequest>[^\"]+)\"\\s" +
            "(?<httpResponse>\\S+)\\s" +
            "(?<size>\\S+)\\s" +
            "\"(?<referrer>[^\"]+)\"\\s" +
            "\"(?<userAgent>[^\"]+)\"");

    public Stream<ParsedLog> parse(Stream<String> stream) {
        return stream.map(log -> parseLine(log));
    }

    private ParsedLog parseLine(String log) {

        Matcher matcher = PATTERN.matcher(log);

        if (!matcher.matches()) {
            try {
                logger.error("Ошибка парсинга. Строка не соответствует паттерну: {}", log);
                throw new IOException("Ошибка парсинга. Строка не соответствует паттерну");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        String ip = matcher.group("id");
        String clientId = matcher.group("clientId");
        String userRFCId = matcher.group("userRFCId");

        ZonedDateTime date = ZonedDateTime.parse(matcher.group("date"), DATE_FORMAT);

        String httpRequest = matcher.group("httpRequest");

        int httpResponse = Integer.parseInt(matcher.group("httpResponse"));
        int size = Integer.parseInt(matcher.group("size"));

        String referrer = matcher.group("referrer");
        String userAgent = matcher.group("userAgent");

        return new ParsedLog(ip, clientId, userRFCId, date, httpRequest, httpResponse, size, referrer, userAgent);
    }
}
