package academy.analytics;

import academy.util.AnalysisContext;
import academy.util.Date;
import academy.util.ParsedLog;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** распределение запросов по датам в процентом соотношении от общего числа */
public class DateDistributionStats implements AnalyzerModule{
    private final Map<ZonedDateTime, Integer> frequency = new HashMap<>();

    @Override
    public void accept(ParsedLog log) {
        addToCounter(log);
    }

    @Override
    public void applyToContext(AnalysisContext context) {
        context.setRequestsPerDate(getRequestsPerDate(context.getTotalRequestsCount()));
    }

    private List<Date> getRequestsPerDate(int totalCount) {
        List<Date> requestsPerDate = new ArrayList<>();
        frequency.forEach((key, value) -> requestsPerDate.add(new Date(
            key.toLocalDate().toString(),
            key.getDayOfWeek().toString(),
            value,
            getTotalRequestsPercentage(totalCount, value)
        )));

        return requestsPerDate;
    }

    private double getTotalRequestsPercentage(int totalCount, int localCount) {
        return Math.round((localCount / (double) totalCount) * 100.0) / 100.0;
    }

    private void addToCounter(ParsedLog log) {
        ZonedDateTime date = log.date();
        ZonedDateTime startOfDay = date.toLocalDate().atStartOfDay(date.getZone()); // нормализую тут время, чтобы запросы по дням группировались
        frequency.put(startOfDay, frequency.getOrDefault(startOfDay, 0) + 1);
    }
}
