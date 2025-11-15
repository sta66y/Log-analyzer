package academy.io.input;

import java.io.IOException;
import java.util.stream.Stream;

/** Читает логи из различных источников.*/
public interface Reader {

    /**
     * Читает данные и возвращает их в виде потока строк.

     * @return поток строк логов
     * @throws RuntimeException если произошла ошибка при чтении
     */
    Stream<String> read() throws IOException, InterruptedException;

    /**
     * Возвращает путь или идентификатор источника данных.
     * Используется для отображения в контексте.
     *
     * @return путь к файлу, URL или другой идентификатор источника
     */
    String getPath();
}
