package academy.parser;

import java.time.ZonedDateTime;

public record ParsedLog(
    String ip,
    String clientId,
    String userRFCId,
    ZonedDateTime date,
    String httpRequest,
    int httpResponse,
    int size,
    String referrer,
    String userAgent
) {}
