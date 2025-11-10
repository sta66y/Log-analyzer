package academy.util;

import java.time.ZonedDateTime;

public record ParsedLog(
    String ip,
    String clientId,
    String userRFCId,
    ZonedDateTime date,
    String method,
    String resource,
    String version,
    int httpResponse,
    int size,
    String referrer,
    String userAgent
) {}
