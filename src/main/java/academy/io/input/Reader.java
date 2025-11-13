package academy.io.input;

import java.util.stream.Stream;

public interface Reader {
    Stream<String> read(String path);
}
