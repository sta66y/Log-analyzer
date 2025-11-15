package academy.analytics;

import academy.model.AnalysisContext;
import academy.model.ParsedLog;

public interface AnalyzerModule {
    void accept(ParsedLog log);
    void applyToContext(AnalysisContext context);
}
