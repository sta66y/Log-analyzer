package academy.io.output;

import academy.enums.OutputFormats;

public class WriterFabric {
    public static Writer createWriter(OutputFormats format) {
        return switch (format) {
            case MD -> new MdWriter();
            case ADOC -> new AdocWriter();
            case JSON -> new JsonWriter();
        };
    }
}
