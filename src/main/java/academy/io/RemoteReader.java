package academy.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.stream.Stream;

public class RemoteReader implements Reader{
    private static final Logger logger = LogManager.getLogger(RemoteReader.class);

    private final HttpClient client;

    public RemoteReader(HttpClient client) {
        this.client = client;
    }

    @Override
    public Stream<String> read(String path) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(path))
                .GET()
                .build();

            HttpResponse<InputStream> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofInputStream()
            );

            if (response.statusCode() != 200) {
                logger.error("Ошибка соединения с сервером: {}", response.statusCode());
                throw new IOException("Ошибка соединения с сервером: " + response.statusCode());
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()));
            return reader.lines();

        } catch (InterruptedException e) {
            logger.error("Прерывание при HTTP-запросе");
            throw new RuntimeException("Прерывание при HTTP-запросе", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
