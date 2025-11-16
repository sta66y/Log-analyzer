package academy.io.input;

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

/** Читает логи из удаленного источника по HTTP/HTTPS. */
public class RemoteReader implements Reader {
    private static final Logger logger = LogManager.getLogger(RemoteReader.class);

    private final HttpClient client;
    private final String path;

    public RemoteReader(HttpClient client, String path) {
        this.client = client;
        this.path = path;
    }

    public RemoteReader(String path) {
        this.path = path;
        this.client = HttpClient.newHttpClient();
    }

    @Override
    public Stream<String> read() throws InterruptedException, IOException {
        try {
            logger.info("Попытка подключения к {}", path);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(path))
                .GET()
                .build();

            HttpResponse<InputStream> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofInputStream()
            );

            logger.info("HTTP статус: {} для URL: {}", response.statusCode(), path);

            if (response.statusCode() != 200) {
                logger.error("Ошибка {} для URL: {}", response.statusCode(), path);
                throw new IOException("Ошибка соединения с сервером: " + response.statusCode());
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()));
            return reader.lines();

        } catch (InterruptedException e) {
            throw new InterruptedException("Прерывание при HTTP-запросе");
        } catch (IOException e) {
            throw new IOException("Ошибка соединения с сервером");
        }
    }

    @Override
    public String getPath() {
        return path;
    }
}
