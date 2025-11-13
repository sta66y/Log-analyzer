package academy.io.output;

import academy.util.AnalysisContext;
import academy.util.Date;
import academy.util.Resource;
import academy.util.ResponseCode;
import academy.util.ResponseSize;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

public class MdWriter implements Writer {
    @Override
    public void write(Path path, AnalysisContext context) {
        StringBuilder md = new StringBuilder();

        md.append("# Анализ логов NGINX\n\n");

        md.append("#### Общая информация\n");
        md.append("| Метрика | Значение |\n");
        md.append("|:--------|---------:|\n");

        String filesDisplay = formatDisplayFiles(context.getFiles());
        md.append("| Файл(-ы) | ").append(filesDisplay).append(" |\n");
        String startDateDisplay = formatDisplayDate(context.getStartDate());
        md.append("| Начальная дата | ").append(startDateDisplay).append(" |\n");
        String endDateDisplay = formatDisplayDate(context.getEndDate());
        md.append("| Конечная дата | ").append(endDateDisplay).append(" |\n");
        String totalRequestsDisplay = formatTotalRequestsDisplay(context.getTotalRequestsCount());
        md.append("| Количество запросов | ").append(totalRequestsDisplay).append(" |\n");
        String maxSizeResponseDisplay = formatResponseSizesDisplay(context.getResponseSizeInBytes(), "max");
        md.append("| Максимальный ответ ответа | ").append(maxSizeResponseDisplay).append(" |\n");
        String averageResponseDisplay = formatResponseSizesDisplay(context.getResponseSizeInBytes(), "average");
        md.append("| Средний размер ответа | ").append(averageResponseDisplay).append(" |\n");
        String p95ResponseDisplay = formatResponseSizesDisplay(context.getResponseSizeInBytes(), "p95");
        md.append("| 95p ответ ответа | ").append(p95ResponseDisplay).append(" |\n\n");//TODO поменяй имена пж

        md.append("#### Запрашиваемые ресурсы\n");
        md.append("| Ресурс | Количество |\n");
        md.append("|:-------|-----------:|\n");

        List<Resource> resources = context.getResources();
        for (Resource resource : resources) {
            md.append("| ").append(resource.resource()).append(" |");
            md.append("| ").append(Integer.toString(resource.totalRequestsCount())).append(" |\n");
        }
        md.append("\n");

        md.append("#### Коды ответа\n");
        md.append("| Код | Имя | Количество |\n");
        md.append("|:----|:---:|-----------:|\n");

        List<ResponseCode> responseCodes = context.getResponseCodes();
        for (ResponseCode responseCode : responseCodes) {
            md.append("| ").append(Integer.toString(responseCode.code())).append(" |");
            md.append("| ").append(getHttpCodeName(responseCode.code())).append(" |");
            md.append("| ").append(Integer.toString(responseCode.totalResponsesCount())).append(" |\n");
        }
        md.append("\n");

        md.append("#### Статистика по датам\n");
        md.append("| Дата | День недели |Количество | Процентное соотношение |\n");
        md.append("|:-----|:-----------:|:---------:|-----------------------:|\n");

        List<Date> dates = context.getRequestsPerDate();
        for (Date date : dates) {
            md.append("| ").append(date.date()).append(" |");
            md.append("| ").append(date.weekday()).append(" |");
            md.append("| ").append(Integer.toString(date.totalRequestsCount())).append(" |");
            md.append("| ").append(Double.toString(date.totalRequestsPercentage())).append(" |\n");
        }
        md.append("\n");

        md.append("#### Статистика по протоколам\n");
        md.append("| Протокол | Количество |\n");
        md.append("|:---------|-----------:|\n");

        Set<String> uniqueProtocols = context.getUniqueProtocols();
        for (String protocol : uniqueProtocols) {
            md.append("| ").append(protocol).append(" |\n");
        }

        try {
            Files.writeString(path, md.toString());
        } catch (IOException e) {
            throw new RuntimeException("Не удалось записать markdown в файл");
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
        throw new RuntimeException("че"); //TODO все таки enum
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
