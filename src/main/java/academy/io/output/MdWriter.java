package academy.io.output;

import academy.model.AnalysisContext;
import academy.model.Date;
import academy.model.Resource;
import academy.model.ResponseCode;
import academy.util.WriterUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/** Записывает данные из контекста в файл в формате markdown*/
public class MdWriter implements Writer {
    @Override
    public void write(Path path, AnalysisContext context) throws IOException {
        StringBuilder md = new StringBuilder();

        md.append("# Анализ логов NGINX\n\n");

        md.append("#### Общая информация\n");
        md.append("| Метрика | Значение |\n");
        md.append("|:--------|---------:|\n");

        String filesDisplay = WriterUtil.formatDisplayFiles(context.getFiles());
        md.append("| Файл(-ы) | ").append(filesDisplay).append(" |\n");
        String startDateDisplay = WriterUtil.formatDisplayDate(context.getDateFrom());
        md.append("| Начальная дата | ").append(startDateDisplay).append(" |\n");
        String endDateDisplay = WriterUtil.formatDisplayDate(context.getDateTo());
        md.append("| Конечная дата | ").append(endDateDisplay).append(" |\n");
        String totalRequestsDisplay = WriterUtil.formatTotalRequestsDisplay(context.getTotalRequestsCount());
        md.append("| Количество запросов | ").append(totalRequestsDisplay).append(" |\n");
        String maxSizeResponseDisplay = WriterUtil.formatResponseSize(context.getResponseSize(), "maxValue");
        md.append("| Максимальный ответ ответа | ").append(maxSizeResponseDisplay).append(" |\n");
        String averageResponseDisplay = WriterUtil.formatResponseSize(context.getResponseSize(), "averageValue");
        md.append("| Средний размер ответа | ").append(averageResponseDisplay).append(" |\n");
        String p95ResponseDisplay = WriterUtil.formatResponseSize(context.getResponseSize(), "p95Value");
        md.append("| 95p ответ ответа | ").append(p95ResponseDisplay).append(" |\n\n");

        md.append("#### Запрашиваемые ресурсы\n");
        md.append("| Ресурс | Количество |\n");
        md.append("|:-------|-----------:|\n");

        List<Resource> resources = context.getResources();
        for (Resource resource : resources) {
            md.append("| ").append(resource.resource());
            md.append("| ").append(resource.totalRequestsCount()).append(" |\n");
        }
        md.append("\n");

        md.append("#### Коды ответа\n");
        md.append("| Код | Имя | Количество |\n");
        md.append("|:----|:---:|-----------:|\n");

        List<ResponseCode> responseCodes = context.getResponseCodes();
        for (ResponseCode responseCode : responseCodes) {
            md.append("| ").append(responseCode.code());
            md.append("| ").append(WriterUtil.getHttpCodeName(responseCode.code()));
            md.append("| ").append(responseCode.totalResponsesCount()).append(" |\n");
        }
        md.append("\n");

        md.append("#### Статистика по датам\n");
        md.append("| Дата | День недели |Количество | Процентное соотношение |\n");
        md.append("|:-----|:-----------:|:---------:|-----------------------:|\n");

        List<Date> dates = context.getRequestsPerDate();
        for (Date date : dates) {
            md.append("| ").append(date.date());
            md.append("| ").append(date.weekday());
            md.append("| ").append(date.totalRequestsCount());
            md.append("| ").append(date.totalRequestsPercentage()).append(" |\n");
        }
        md.append("\n");

        md.append("#### Уникальные протоколы\n");
        md.append("| Протокол |\n");
        md.append("|:--------:|\n");
        Set<String> uniqueProtocols = context.getUniqueProtocols();
        for (String protocol : uniqueProtocols) {
            md.append("| ").append(protocol).append(" |\n");
        }

        if (context.getDateFrom() != null) md.append("\nНачальная дата: ").append(context.getDateFrom().toString());
        if (context.getDateTo() != null) md.append("\nКонечная дата: ").append(context.getDateTo().toString());


        try {
            Files.writeString(path, md.toString());
        } catch (IOException e) {
            throw new IOException("Не удалось записать markdown в файл");
        }
    }
}
