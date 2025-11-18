package academy.io.output;

import academy.model.AnalysisContext;
import academy.model.Date;
import academy.model.Resource;
import academy.model.ResponseCode;
import academy.model.ResponseSize;
import academy.util.WriterUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/** Записывает данные из контекста в файл в формате adoc */
public class AdocWriter implements Writer {

    @Override
    public void write(Path path, AnalysisContext context) throws IOException {
        StringBuilder adoc = new StringBuilder();

        adoc.append("= Анализ логов NGINX\n");
        adoc.append(":toc:\n");
        adoc.append(":toclevels: 3\n\n");

        adoc.append("==== Общая информация\n\n");
        adoc.append("[cols=\"1,1\", options=\"header\"]\n");
        adoc.append("|===\n");
        adoc.append("| Метрика | Значение\n");

        adoc.append("| Файл(-ы) | ")
                .append(WriterUtil.formatDisplayFiles(context.getFiles()))
                .append("\n");
        adoc.append("| Начальная дата | ")
                .append(WriterUtil.formatDisplayDate(context.getDateFrom()))
                .append("\n");
        adoc.append("| Конечная дата | ")
                .append(WriterUtil.formatDisplayDate(context.getDateTo()))
                .append("\n");
        adoc.append("| Количество запросов | ")
                .append(WriterUtil.formatTotalRequestsDisplay(context.getTotalRequestsCount()))
                .append("\n");

        ResponseSize size = context.getResponseSizeInBytes();
        if (size != null) {
            adoc.append("| Максимальный размер ответа | ")
                    .append(WriterUtil.formatResponseSize(size, "maxValue"))
                    .append("\n");
            adoc.append("| Средний размер ответа | ")
                    .append(WriterUtil.formatResponseSize(size, "averageValue"))
                    .append("\n");
            adoc.append("| 95p размера ответа | ")
                    .append(WriterUtil.formatResponseSize(size, "p95Value"))
                    .append("\n");
        }

        adoc.append("|===\n\n");

        adoc.append("==== Запрашиваемые ресурсы\n\n");
        adoc.append("[cols=\"3,1\", options=\"header\"]\n");
        adoc.append("|===\n");
        adoc.append("| Ресурс | Количество\n");

        List<Resource> resources = context.getResources();
        for (Resource resource : resources) {
            adoc.append("| `")
                    .append(resource.resource())
                    .append("` | ")
                    .append(WriterUtil.formatTotalRequestsDisplay(resource.totalRequestsCount()))
                    .append("\n");
        }
        adoc.append("|===\n\n");

        adoc.append("==== Коды ответа\n\n");
        adoc.append("[cols=\"1,2,1\", options=\"header\"]\n");
        adoc.append("|===\n");
        adoc.append("| Код | Имя | Количество\n");

        List<ResponseCode> responseCodes = context.getResponseCodes();
        for (ResponseCode responseCode : responseCodes) {
            adoc.append("| ")
                    .append(WriterUtil.formatTotalRequestsDisplay(responseCode.code()))
                    .append(" | ")
                    .append(WriterUtil.getHttpCodeName(responseCode.code()))
                    .append(" | ")
                    .append(WriterUtil.formatTotalRequestsDisplay(responseCode.totalResponsesCount()))
                    .append("\n");
        }
        adoc.append("|===\n\n");

        if (!context.getRequestsPerDate().isEmpty()) {
            adoc.append("==== Статистика по датам\n\n");
            adoc.append("[cols=\"1,1,1,1\", options=\"header\"]\n");
            adoc.append("|===\n");
            adoc.append("| Дата | День недели | Количество | Доля\n");

            List<Date> dates = context.getRequestsPerDate();
            for (Date date : dates) {
                adoc.append("| ")
                        .append(date.date())
                        .append(" | ")
                        .append(date.weekday())
                        .append(" | ")
                        .append(WriterUtil.formatTotalRequestsDisplay(date.totalRequestsCount()))
                        .append(" | ")
                        .append(date.totalRequestsPercentage())
                        .append("% |\n");
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

        if (context.getDateFrom() != null)
            adoc.append("\nНачальная дата: ").append(context.getDateFrom().toString());
        if (context.getDateTo() != null)
            adoc.append("\nКонечная дата: ").append(context.getDateTo().toString());

        try {
            Files.writeString(path, adoc.toString());
        } catch (IOException e) {
            throw new IOException("Не удалось записать AsciiDoc в файл", e);
        }
    }
}
