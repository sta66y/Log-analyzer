package academy.analytics;

import academy.model.AnalysisContext;
import academy.model.ParsedLog;
import academy.model.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Выводит топ-10 наиболее часто запрашиваемых ресурсов, отсортированных по убыванию */
public class TopResourcesStats implements AnalyzerModule {
    private final Map<String, Integer> frequency = new HashMap<>();


    @Override
    public void accept(ParsedLog log) {
        addToCounter(log);
    }

    @Override
    public void applyToContext(AnalysisContext context) {
        context.setResources(getResources());
    }

    private List<Resource> getResources() {
        List<Resource> resources = new ArrayList<>(10);
        frequency.forEach((key, value) -> resources.add(new Resource(key, value)));
        return resources.stream()
            .sorted(Comparator.comparingInt(Resource::totalRequestsCount).reversed())
            .limit(10) //TODO мб вынести в конфиг
            .collect(Collectors.toList());
    }

    private void addToCounter(ParsedLog log) {
        String resource = log.resource();
        frequency.put(resource, frequency.getOrDefault(resource, 0) + 1);
    }
}
