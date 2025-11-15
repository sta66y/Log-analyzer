package academy.util;

import academy.enums.OutputFormats;
import academy.io.output.AdocWriter;
import academy.io.output.JsonWriter;
import academy.io.output.MdWriter;
import academy.io.output.Writer;

/** Фабрика для создания writer'ов в зависимости от формата вывода. */
public class WriterFactory {

    /**
     * Создает writer для указанного формата вывода.
     *
     * @param format формат вывода результатов
     * @return соответствующий writer
     */
    public static Writer createWriter(OutputFormats format) {
        return switch (format) {
            case MD -> new MdWriter();
            case ADOC -> new AdocWriter();
            case JSON -> new JsonWriter();
        };
    }
}
