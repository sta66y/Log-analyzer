package academy.analytics;

import academy.model.AnalysisContext;
import academy.model.Date;
import academy.model.ParsedLog;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Анализирует распределение запросов по датам в процентом соотношении от общего числа<br>
 * Собирает статистику по распределению запросов по датам. Группирует запросы по дням и считает их количество.
 */
public class AnalyzeDateDistribution implements AnalyzerModule {
    private final Map<ZonedDateTime, Integer> frequencyRequestsPerDate = new HashMap<>();

    @Override
    public void accept(ParsedLog log) {
        addLogToCounter(log);
    }

    @Override
    public void applyToContext(AnalysisContext context) {
        int totalRequestCount = context.getTotalRequestsCount();
        if (totalRequestCount == 0) return; // обработка случая, когда нет логов
        context.setRequestsPerDate(getRequestsPerDate(context.getTotalRequestsCount()));
    }

    private List<Date> getRequestsPerDate(int totalCount) {
        return frequencyRequestsPerDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> createDateEntry(entry.getKey(), entry.getValue(), totalCount))
                .collect(Collectors.toList());
    }

    private Date createDateEntry(ZonedDateTime date, int count, int totalCount) {
        String weekday = date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH);
        String capitalizedWeekday =
                weekday.substring(0, 1).toUpperCase() + weekday.substring(1).toLowerCase();

        return new Date(
                date.toLocalDate().toString(), capitalizedWeekday, count, calculatePercentage(totalCount, count));
    }

    private double calculatePercentage(int totalCount, int localCount) {
        return Math.round(localCount / (double) totalCount * 10000.0) / 100.0;
    }

    private void addLogToCounter(ParsedLog log) {
        ZonedDateTime startOfDay =
                log.date().toLocalDate().atStartOfDay(log.date().getZone());
        frequencyRequestsPerDate.merge(startOfDay, 1, Integer::sum);
    }
}
