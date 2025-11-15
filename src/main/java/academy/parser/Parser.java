package academy.parser;

import academy.util.ParsedLog;
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
            "\"(?<method>\\S+)\\s" +
            "(?<resource>\\S+)\\s" +
            "(?<version>\\S+)\"\\s" +
            "(?<httpResponse>\\S+)\\s" +
            "(?<size>\\S+)\\s" +
            "\"(?<referrer>[^\"]+)\"\\s" +
            "\"(?<userAgent>[^\"]+)\"");

    public ParsedLog parseLine(String log) {

        Matcher matcher = PATTERN.matcher(log);

        if (!matcher.matches()) {
            try {
                logger.error("Ошибка парсинга. Строка не соответствует паттерну: {}", log);
                throw new IOException("Ошибка парсинга. Строка не соответствует паттерну: " + log);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        String ip = matcher.group("id");
        String clientId = matcher.group("clientId");
        String userRFCId = matcher.group("userRFCId");

        ZonedDateTime date = ZonedDateTime.parse(matcher.group("date"), DATE_FORMAT);

        String method = matcher.group("method");
        String resource = matcher.group("resource");
        String version = matcher.group("version");

        int httpResponse = Integer.parseInt(matcher.group("httpResponse"));
        int size = Integer.parseInt(matcher.group("size"));

        String referrer = matcher.group("referrer");
        String userAgent = matcher.group("userAgent");

        return new ParsedLog(ip, clientId, userRFCId, date, method,
            resource, version, httpResponse, size, referrer, userAgent);
    }
}
