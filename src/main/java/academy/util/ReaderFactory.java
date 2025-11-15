package academy.util;

import academy.io.input.LocalReader;
import academy.io.input.Reader;
import academy.io.input.RemoteReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;

public class ReaderFactory {
    private static final Logger logger = LogManager.getLogger(ReaderFactory.class);

    public static Reader createReader(String path) throws IOException {
        if (path.startsWith("http")) {
            logger.info("Создан RemoteReader для: {}", path);
            return new RemoteReader(path);
        } else {
            logger.info("Создан LocalReader для: {}", path);
            return new LocalReader(path);
        }
    }
}
