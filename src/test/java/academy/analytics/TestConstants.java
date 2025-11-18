package academy.analytics;

import academy.model.ParsedLog;
import java.time.ZonedDateTime;
import java.util.List;

public class TestConstants {
    static final List<ParsedLog> EXAMPLES_LOG = List.of(
            new ParsedLog(
                    "192.168.1.100",
                    "-",
                    "frank",
                    ZonedDateTime.parse("2024-10-15T14:30:25Z"),
                    "GET",
                    "/index.html",
                    "HTTP/1.1",
                    200,
                    1024,
                    "-",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"),
            new ParsedLog(
                    "203.0.113.195",
                    "client123",
                    "james",
                    ZonedDateTime.parse("2023-10-15T14:35:10Z"),
                    "GET",
                    "/nonexistent-page.html",
                    "HTTP/1.1",
                    404,
                    512,
                    "https://example.com",
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15"),
            new ParsedLog(
                    "198.51.100.23",
                    "-",
                    "mary",
                    ZonedDateTime.parse("2023-10-15T14:40:00Z"),
                    "POST",
                    "/api/login",
                    "HTTP/1.1",
                    302,
                    0,
                    "https://mysite.com/login",
                    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36"),
            new ParsedLog(
                    "66.249.66.1",
                    "-",
                    "-",
                    ZonedDateTime.parse("2023-10-15T14:45:30Z"),
                    "GET",
                    "/index.html",
                    "HTTP/1.1",
                    200,
                    1234,
                    "-",
                    "Googlebot/2.1"),
            new ParsedLog(
                    "10.0.0.55",
                    "webapp-user",
                    "admin",
                    ZonedDateTime.parse("2023-10-15T14:50:15Z"),
                    "PUT",
                    "/api/users/123",
                    "HTTP/2.0",
                    500,
                    0,
                    "https://admin.mysite.com/users",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/118.0"));
}
