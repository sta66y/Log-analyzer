package academy.model;

/** Статистика по HTTP коду ответа. */
public record ResponseCode(
    int code,
    int totalResponsesCount
) {}
