package academy.analytics;

import academy.model.AnalysisContext;
import academy.model.ParsedLog;
import academy.model.ResponseCode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Собирает статистику по HTTP кодам ответов. Подсчитывает частоту каждого кода состояния в логах. */
public class AnalyzeResponseCodesFrequency implements AnalyzerModule {
    private final Map<Integer, Integer> frequencyResponseCodes = new HashMap<>();

    @Override
    public void accept(ParsedLog log) {
        addToCounter(log);
    }

    @Override
    public void applyToContext(AnalysisContext context) {
        context.setResponseCodes(getResponseCodes());
    }

    /** Преобразует собранную статистику в список кодов ответов. */
    private List<ResponseCode> getResponseCodes() {
        List<ResponseCode> responseCodes = new ArrayList<>();
        frequencyResponseCodes.forEach((code, count) -> responseCodes.add(new ResponseCode(code, count)));
        return responseCodes;
    }

    private void addToCounter(ParsedLog log) {
        int response = log.httpResponse();
        frequencyResponseCodes.put(response, frequencyResponseCodes.getOrDefault(response, 0) + 1);
    }
}
