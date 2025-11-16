package academy.analytics;

import academy.model.AnalysisContext;
import academy.model.ParsedLog;
import academy.model.ResponseSize;
import java.util.ArrayList;
import java.util.List;

/** Cчитает средний, максимальный и 95-й перцентиль размера ответа.*/
public class AnalyzeResponseSize implements AnalyzerModule {
    private final List<Integer> responses = new ArrayList<>();
    private int sumResponses = 0;
    private int maxResponse = 0;

    @Override
    public void accept(ParsedLog log) {
        addResponse(log);
    }

    @Override
    public void applyToContext(AnalysisContext context) {
        responses.sort(Integer::compareTo);

        int totalRequestCount = context.getTotalRequestsCount();
        if (totalRequestCount == 0) return; // обработка случая, когда нет логов

        context.setResponseSizeInBytes(new ResponseSize(
            calculateAverage(context.getTotalRequestsCount()),
            maxResponse,
            calculateP95(context.getTotalRequestsCount())
        ));
    }

    private double calculateAverage(int totalRequests) {
        return Math.round((sumResponses / (double) totalRequests) * 100.0) / 100.0;
    }

    private int calculateP95(int totalRequests) {
        int index = (int) Math.ceil(0.95 * totalRequests) - 1;
        return responses.get(Math.max(index, 0));
    }

    private void addResponse(ParsedLog log) {
        int responseSize = log.httpResponse();
        responses.add(responseSize);
        sumResponses += responseSize;
        maxResponse = Math.max(maxResponse, responseSize);
    }
}
