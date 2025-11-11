package academy.analytics;

import academy.util.AnalysisContext;
import academy.util.ParsedLog;
import academy.util.ResponseSize;
import java.util.ArrayList;
import java.util.List;

/** Cчитает средний, максимальный и 95-й перцентиль размера ответа.*/
public class ResponseSizeStats implements AnalyzerModule{
    private final List<Integer> answers = new ArrayList<>();
    private int sumResponses = 0;
    private int maxResponse = 0;

    @Override
    public void accept(ParsedLog log){
        addAnswer(log);
        increaseSumResponses(log);
        checkMaxResponse(log);
    }

    @Override
    public void applyToContext(AnalysisContext context) {
        context.setResponseSizeInBytes(new ResponseSize(
            getAverage(context.getTotalRequestsCount()),
            maxResponse,
            getP95(context.getTotalRequestsCount())));
    }

    private void sortAnswers() {
        answers.sort(Integer::compareTo);
    }

    private double getAverage(int size) {
        return Math.round((sumResponses / (double) size) * 100.0) / 100.0;
    }

    private int getP95(int size) {
        sortAnswers();
        int index = (int) Math.ceil(0.95 * size) - 1;
        return answers.get(Math.max(index, 0));
    }

    private void addAnswer(ParsedLog log) {
        answers.add(log.httpResponse());
    }

    private void increaseSumResponses(ParsedLog log) {
        sumResponses += log.httpResponse();
    }

    private void checkMaxResponse(ParsedLog log) {
        maxResponse = Math.max(log.httpResponse(), maxResponse);
    }
}
