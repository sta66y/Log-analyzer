package academy.analytics;

import academy.model.AnalysisContext;
import academy.model.ParsedLog;
import academy.model.ResponseSize;
import java.util.ArrayList;
import java.util.List;

/** Cчитает средний, максимальный и 95-й перцентиль размера ответа. */
public class AnalyzeResponseSize implements AnalyzerModule {
    private final List<Integer> responseSizes = new ArrayList<>();
    private int sumResponses = 0;
    private int maxResponse = 0;

    @Override
    public void accept(ParsedLog log) {
        addResponse(log);
    }

    @Override
    public void applyToContext(AnalysisContext context) {
        responseSizes.sort(Integer::compareTo);

        int totalRequestCount = context.getTotalRequestsCount();
        if (totalRequestCount == 0) return; // обработка случая, когда нет логов

        context.setResponseSizeInBytes(new ResponseSize(
                calculateAverage(context.getTotalRequestsCount()),
                maxResponse,
                calculateP95(context.getTotalRequestsCount())));
    }

    private double calculateAverage(int totalRequests) {
        return Math.round((sumResponses / (double) totalRequests) * 100.0) / 100.0;
    }

    private double calculateP95(int totalRequests) {
        if (responseSizes.isEmpty()) return 0;

        double position = 0.95 * (totalRequests - 1);
        int index = (int) position;

        if (position > index) {
            double fraction = position - index;
            int value1 = responseSizes.get(index);
            int value2 = responseSizes.get(index + 1);
            return (int) Math.round(value1 + fraction * (value2 - value1));
        } else {
            return responseSizes.get(index);
        }
    }

    private void addResponse(ParsedLog log) {
        int responseSize = log.size();
        responseSizes.add(responseSize);
        sumResponses += responseSize;
        maxResponse = Math.max(maxResponse, responseSize);
    }
}
