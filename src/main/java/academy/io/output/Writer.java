package academy.io.output;

import academy.model.AnalysisContext;
import java.nio.file.Path;

public interface Writer {
    public void write(Path path, AnalysisContext context);
}
