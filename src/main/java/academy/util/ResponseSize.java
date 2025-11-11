package academy.util;

public record ResponseSize(
    double average,
    int max,
    int p95
) {}
