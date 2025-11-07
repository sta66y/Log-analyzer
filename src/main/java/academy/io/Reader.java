package academy.io;

import java.util.stream.Stream;

public interface Reader {
    Stream<String> read(String path);
}
