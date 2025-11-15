package academy.model;

import java.time.ZonedDateTime;

/** Распарсенная запись лога */
public record ParsedLog(
    String ip,
    String clientId,
    String userRFCId,
    ZonedDateTime date,
    String method,
    String resource,
    String protocol,
    int httpResponse,
    int size,
    String referrer,
    String userAgent
) {}
