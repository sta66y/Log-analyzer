package academy.io.output;

import academy.model.AnalysisContext;
import java.io.IOException;
import java.nio.file.Path;

/** Записывает результаты анализа в файл. */
public interface Writer {
    void write(Path path, AnalysisContext context) throws IOException;
}
