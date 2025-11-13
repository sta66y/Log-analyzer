package academy.io.output;

import academy.util.AnalysisContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonWriter implements Writer {
    @Override
    public void write(Path path, AnalysisContext context) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            String json = mapper.writeValueAsString(context);
            Files.writeString(path, json);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось записать json в файл");
        }
    }
}
