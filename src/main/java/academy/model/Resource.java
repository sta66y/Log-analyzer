package academy.model;

/** Статистика по ресурсу. */
public record Resource (
    String resource,
    int totalRequestsCount
) {}
