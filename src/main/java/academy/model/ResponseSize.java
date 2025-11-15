package academy.model;

public record ResponseSize(
    double average,
    int max,
    int p95
) {}
