package academy.io.output;

import academy.model.AnalysisContext;
import academy.model.Date;
import academy.model.Resource;
import academy.model.ResponseCode;
import academy.model.ResponseSize;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class AdocWriter implements Writer {

    @Override
    public void write(Path path, AnalysisContext context) {
        StringBuilder adoc = new StringBuilder();

        adoc.append("= Анализ логов NGINX\n");
        adoc.append(":toc:\n");
        adoc.append(":toclevels: 3\n\n");

        adoc.append("==== Общая информация\n\n");
        adoc.append("[cols=\"1,1\", options=\"header\"]\n");
        adoc.append("|===\n");
        adoc.append("| Метрика | Значение\n");

        adoc.append("| Файл(-ы) | ").append(formatDisplayFiles(context.getFiles())).append("\n");
        adoc.append("| Начальная дата | ").append(formatDisplayDate(context.getStartDate())).append("\n");
        adoc.append("| Конечная дата | ").append(formatDisplayDate(context.getEndDate())).append("\n");
        adoc.append("| Количество запросов | ").append(formatTotalRequestsDisplay(context.getTotalRequestsCount())).append("\n");

        ResponseSize size = context.getResponseSizeInBytes();
        if (size != null) {
            adoc.append("| Максимальный размер ответа | ").append(formatResponseSizesDisplay(size, "max")).append("\n");
            adoc.append("| Средний размер ответа | ").append(formatResponseSizesDisplay(size, "average")).append("\n");
            adoc.append("| 95p размера ответа | ").append(formatResponseSizesDisplay(size, "p95")).append("\n");
        }

        adoc.append("|===\n\n");

        adoc.append("==== Запрашиваемые ресурсы\n\n");
        adoc.append("[cols=\"3,1\", options=\"header\"]\n");
        adoc.append("|===\n");
        adoc.append("| Ресурс | Количество\n");

        List<Resource> resources = context.getResources();
        for (Resource resource : resources) {
            adoc.append("| `").append(resource.resource()).append("` | ")
                .append(formatTotalRequestsDisplay(resource.totalRequestsCount())).append("\n");
        }
        adoc.append("|===\n\n");

        adoc.append("==== Коды ответа\n\n");
        adoc.append("[cols=\"1,2,1\", options=\"header\"]\n");
        adoc.append("|===\n");
        adoc.append("| Код | Имя | Количество\n");

        List<ResponseCode> responseCodes = context.getResponseCodes();
        for (ResponseCode responseCode : responseCodes) {
            adoc.append("| ").append(formatTotalRequestsDisplay(responseCode.code())).append(" | ")
                .append(getHttpCodeName(responseCode.code())).append(" | ")
                .append(formatTotalRequestsDisplay(responseCode.totalResponsesCount())).append("\n");
        }
        adoc.append("|===\n\n");

        if (!context.getRequestsPerDate().isEmpty()) {
            adoc.append("==== Статистика по датам\n\n");
            adoc.append("[cols=\"1,1,1,1\", options=\"header\"]\n");
            adoc.append("|===\n");
            adoc.append("| Дата | День недели | Количество | Доля\n");

            List<Date> dates = context.getRequestsPerDate();
            for (Date date : dates) {
                adoc.append("| ").append(date.date()).append(" | ")
                    .append(date.weekday()).append(" | ")
                    .append(formatTotalRequestsDisplay(date.totalRequestsCount())).append(" | ")
                    .append(date.totalRequestsPercentage()).append("% |\n");
            }
            adoc.append("|===\n\n");
        }

        adoc.append("==== Статистика по протоколам\n\n");
        adoc.append("[cols=\"1,1\", options=\"header\"]\n");
        adoc.append("|===\n");
        adoc.append("| Протокол\n");

        Set<String> uniqueProtocols = context.getUniqueProtocols();
        for (String protocol : uniqueProtocols) {
            adoc.append("| ").append(protocol).append("\n");
        }
        adoc.append("|===\n");

        if (context.getStartDate() != null) adoc.append("\nНачальная дата: ").append(context.getStartDate().toString());
        if (context.getEndDate() != null) adoc.append("\nКонечная дата: ").append(context.getEndDate().toString());

        try {
            Files.writeString(path, adoc.toString());
        } catch (IOException e) {
            throw new RuntimeException("Не удалось записать AsciiDoc в файл", e);
        }
    }

    private String getHttpCodeName(int code) {
        return switch (code) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 301 -> "Moved Permanently";
            case 302 -> "Found";
            case 304 -> "Not Modified";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 405 -> "Method Not Allowed";
            case 500 -> "Internal Server Error";
            case 502 -> "Bad Gateway";
            case 503 -> "Service Unavailable";
            default -> "Unknown";
        };
    }

    private String formatResponseSizesDisplay(ResponseSize responseSize, String kind) {

        if (kind.equals("max")) return Integer.toString(responseSize.max());
        if (kind.equals("average")) return Double.toString(responseSize.average());
        if (kind.equals("p95")) return Integer.toString(responseSize.p95());
        throw new RuntimeException("че"); // TODO изменить возвращаемое значение
    }

    private String formatTotalRequestsDisplay(int totalRequestsCount) {
        return Integer.toString(totalRequestsCount);
    }

    private String formatDisplayDate(LocalDate startDate) {
        if (startDate == null) return "-";
        return startDate.toString();
    }

    private String formatDisplayFiles(List<String> files) {
        if (files.isEmpty()) return "-";
        if (files.size() == 1) return "`" + files.getFirst() + "`";
        if (files.size() == 2) return "`" + files.getFirst() + "`, `" + files.getLast() + "`";
        return "`" + files.getFirst() + "` и еще " + (files.size() - 1) + " файлов";
    }
}
