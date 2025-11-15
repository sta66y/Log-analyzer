package academy.analytics;

import academy.model.AnalysisContext;
import academy.model.ParsedLog;
import java.util.HashSet;
import java.util.Set;
/**
 * Собирает статистику по уникальным протоколам в логах. <br>
 * Подсчитывает, какие HTTP протоколы используются в запросах.
 */
public class AnalyzeUniqueProtocols implements AnalyzerModule{
    private final Set<String> protocols = new HashSet<>();

    @Override
    public void accept(ParsedLog log) {
        protocols.add(log.protocol());
    }

    @Override
    public void applyToContext(AnalysisContext context) {
        context.setUniqueProtocols(new HashSet<>(protocols));
    }
}
