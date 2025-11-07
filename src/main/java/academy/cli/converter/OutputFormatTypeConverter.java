package academy.cli.converter;

import academy.enums.OutputFormats;
import picocli.CommandLine.ITypeConverter;

public class OutputFormatTypeConverter implements ITypeConverter<OutputFormats> {

    @Override
    public OutputFormats convert(String value) throws Exception {
        return OutputFormats.fromId(value);
    }
}
