package academy.parser;

import academy.model.ParsedLog;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Парсит строки логов NGINX в структурированный формат.` */
public class PathParser {

    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("d/MMM/yyyy:HH:mm:ss Z");

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

    /**
     * Парсит строку лога в структурированный объект.
     *
     * @param log строка лога в формате NGINX
     * @return распарсенная запись лога
     * @throws IOException если строка не соответствует ожидаемому формату
     */
    public ParsedLog parseLine(String log) throws IOException {

        Matcher matcher = PATTERN.matcher(log);

        if (!matcher.matches()) {
            throw new IOException("Ошибка парсинга. Строка не соответствует паттерну: " + log);
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
