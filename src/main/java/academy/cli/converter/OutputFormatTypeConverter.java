package academy.cli.converter;

import academy.enums.OutputFormats;
import picocli.CommandLine.ITypeConverter;

/**
 * Конвертирует строку в enum OutputFormats.<br>
 * Используется picocli для преобразования аргументов командной строки.
 */
public class OutputFormatTypeConverter implements ITypeConverter<OutputFormats> {

    @Override
    public OutputFormats convert(String value) throws Exception {
        return OutputFormats.fromId(value);
    }
}
