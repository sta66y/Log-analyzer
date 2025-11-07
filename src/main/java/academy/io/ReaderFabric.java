package academy.io;

import java.net.http.HttpClient;

public class ReaderFabric {
    public static Reader createReader(String path) {
        if (path.startsWith("http")) return new RemoteReader(HttpClient.newHttpClient());
        else return new LocalReader();
    }
}
