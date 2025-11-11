package academy.analytics;

import academy.util.AnalysisContext;
import academy.util.ParsedLog;

public interface AnalyzerModule {
    void accept(ParsedLog log);
    void applyToContext(AnalysisContext context);
}
