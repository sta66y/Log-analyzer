package academy.io.input;

import org.junit.jupiter.api.Test;
import static academy.io.input.ReaderFabric.createReader;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class ReaderFabricTest {

    @Test
    void createReader_ShouldReturnRemoteReader_WhenPathStartsWithHttp() {
        String path = "http://example.com";
        Reader reader = createReader(path);

        assertInstanceOf(RemoteReader.class, reader, "Reader не RemoteReader");
    }

    @Test
    void createReader_ShouldReturnLocalReader_WhenPathDoesntStartsWithHttp() {
        String path = "/bin";
        Reader reader = createReader(path);

        assertInstanceOf(LocalReader.class, reader, "Reader не LocalReader");
    }

}
