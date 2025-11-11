package academy.analytics;

import academy.util.AnalysisContext;
import academy.util.ParsedLog;
import java.util.HashSet;
import java.util.Set;
/** уникальные используемые протоколы передачи данных */
public class ProtocolStats implements AnalyzerModule{
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
