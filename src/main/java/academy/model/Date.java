package academy.model;

public record Date (
    String date,
    String weekday,
    int totalRequestsCount,
    double totalRequestsPercentage
){}
