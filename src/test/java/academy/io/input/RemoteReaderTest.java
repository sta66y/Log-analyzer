package academy.io.input;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.http.HttpClient;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RemoteReaderTest {
    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    @DisplayName("Должен возвращать поток, если ответ 200")
    void read_ShouldReturnStream_WhenHttp200() throws IOException {
        try (MockWebServer server = new MockWebServer()) {
            server.start();

            server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("line1\nline2\nline3"));

            String url = server.url("/test.log").toString();
            Reader reader = new RemoteReader(client, url);
            Stream<String> stream = reader.read();
            List<String> lines = stream.collect(Collectors.toList());

            assertEquals(List.of("line1", "line2", "line3"), lines);
        }
    }

    @Test
    @DisplayName("Должен выбрасывать ошибку, если ответ не 200")
    void read_ShouldThrowException_WhenHttpNot200() throws IOException {
        try (MockWebServer server = new MockWebServer()) {
            server.start();

            server.enqueue(new MockResponse().setResponseCode(500));

            String url = server.url("/test.log").toString();
            Reader reader = new RemoteReader(client, url);

            RuntimeException ex = assertThrows(RuntimeException.class, () -> reader.read());
            assertTrue(ex.getMessage().contains("Ошибка соединения с сервером"));
        }
    }
}
