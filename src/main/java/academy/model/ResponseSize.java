package academy.model;

/** Статистика размеров ответов. */
public record ResponseSize(
    double averageValue,
    int maxValue,
    int p95Value
) {}
