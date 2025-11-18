package academy.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Статистика размеров ответов. */
public record ResponseSize(
        @JsonProperty("average") double averageValue,
        @JsonProperty("max") double maxValue,
        @JsonProperty("p95") double p95Value) {}
