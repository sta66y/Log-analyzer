package academy.model;

/** Статистика запросов по дате. */
public record Date(String date, String weekday, int totalRequestsCount, double totalRequestsPercentage) {}
