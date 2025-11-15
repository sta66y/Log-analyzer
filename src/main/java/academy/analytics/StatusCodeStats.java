package academy.analytics;

import academy.model.AnalysisContext;
import academy.model.ParsedLog;
import academy.model.ResponseCode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Считает частоту встречаемых кодов ответа */
public class StatusCodeStats implements AnalyzerModule{
    private final Map<Integer, Integer> frequency = new HashMap<>();

    @Override
    public void accept(ParsedLog log) {
        addToCounter(log);
    }

    @Override
    public void applyToContext(AnalysisContext context) {
        context.setResponseCodes(getResponseCodes());
    }

    private List<ResponseCode> getResponseCodes() {
        List<ResponseCode> responseCodes = new ArrayList<>();
        frequency.forEach((key, value) -> responseCodes.add(new ResponseCode(key, value)));
        return responseCodes;
    }

    private void addToCounter(ParsedLog log) {
        int response = log.httpResponse();
        frequency.put(response, frequency.getOrDefault(response, 0) + 1);
    }
}
