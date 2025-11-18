package academy.analytics;

import academy.io.input.Reader;
import academy.model.AnalysisContext;
import academy.model.ParsedLog;
import academy.parser.LogsParser;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Анализатор логов NGINX.
 *
 * <p>Основной класс, отвечающий за координацию процесса анализа логов: создание контекста, парсинг логов, сбор
 * статистики и формирование результатов. Использует модульную систему анализа через {@link AnalyzerModule}.
 *
 * <p><b>Порядок выполнения анализа:</b>
 *
 * <ol>
 *   <li>Создание контекста анализа с метаинформацией
 *   <li>Парсинг и фильтрация строк логов по дате
 *   <li>Сбор статистики через цепочку модулей анализа
 *   <li>Заполнение контекста результатами анализа
 * </ol>
 *
 * <p><b>Важно:</b> порядок модулей анализа имеет значение. {@link AnalyzeTotalCountRequests} должен быть первым, так
 * как он сохраняет общее количество запросов, которое используется другими модулями.
 */
public class Analyzer {
    private static final Logger logger = LogManager.getLogger(Analyzer.class);

    private final List<AnalyzerModule> analyzerModules;
    private final LogsParser logsParser;

    /**
     * @param analyzerModules список модулей анализа в порядке их выполнения
     * @param logsParser парсер для преобразования сырых логов в структурированный формат
     */
    public Analyzer(List<AnalyzerModule> analyzerModules, LogsParser logsParser) {
        this.analyzerModules = analyzerModules;
        this.logsParser = logsParser;
    }
    /** @param logsParser парсер для преобразования сырых логов в структурированный формат */
    public Analyzer(LogsParser logsParser) {
        this.analyzerModules = List.of(
                new AnalyzeTotalCountRequests(),
                new AnalyzeResponseSize(),
                new AnalyzeResponseCodesFrequency(),
                new AnalyzeTopMostFrequentResources(),
                new AnalyzeDateDistribution(),
                new AnalyzeUniqueProtocols());
        this.logsParser = logsParser;
    }
    /**
     * Выполняет полный анализ логов из указанных источников.
     *
     * <p>Процесс анализа включает:
     *
     * <ul>
     *   <li>Фильтрацию логов по указанному временному диапазону
     *   <li>Парсинг строк логов в структурированный формат
     *   <li>Сбор статистики через все зарегистрированные модули анализа
     *   <li>Формирование итогового контекста с результатами
     * </ul>
     *
     * @param readers список источников логов для анализа
     * @param dateFrom начальная дата диапазона анализа (включительно). Если {@code null}, фильтрация по начальной дате
     *     не применяется
     * @param dateTo конечная дата диапазона анализа (включительно). Если {@code null}, фильтрация по конечной дате не
     *     применяется
     * @return контекст анализа с собранной статистикой и метаинформацией
     * @throws IOException если произошла ошибка ввода-вывода при чтении логов
     */
    public AnalysisContext analyse(List<Reader> readers, LocalDate dateFrom, LocalDate dateTo) throws IOException {
        logger.info("Создание контекста");
        AnalysisContext context = createContext(readers, dateFrom, dateTo);

        logger.info("Парсинг и фильтрация строк");
        Stream<ParsedLog> parsedLogs = logsParser.parseAndFilterLogs(readers, dateFrom, dateTo);
        logger.info("Сбор статистики");
        analyzeLogs(parsedLogs);
        logger.info("Заполнение результатов в контекст");
        applyResultsToContext(context);

        return context;
    }
    /**
     * Обрабатывает поток распарсенных логов через все модули анализа.
     *
     * @param parsedLogs поток структурированных логов для анализа
     */
    private void analyzeLogs(Stream<ParsedLog> parsedLogs) {
        parsedLogs.forEach(log -> analyzerModules.forEach(module -> module.accept(log)));
    }
    /**
     * Применяет результаты анализа всех модулей к контексту.
     *
     * @param context контекст анализа для заполнения результатами
     */
    private void applyResultsToContext(AnalysisContext context) {
        analyzerModules.forEach(module -> module.applyToContext(context));
    }
    /**
     * Создает начальный контекст анализа с метаинформацией. <br>
     * Инициализирует контекст путями к исходным файлам и диапазоном дат анализа *
     *
     * @param readers список источников логов
     * @param dateFrom начальная дата анализа
     * @param dateTo конечная дата анализа
     * @return подготовленный контекст анализа
     */
    private AnalysisContext createContext(List<Reader> readers, LocalDate dateFrom, LocalDate dateTo) {
        AnalysisContext context = new AnalysisContext();
        List<String> fileNames = readers.stream()
                .map(reader -> {
                    String path = reader.getPath();
                    if (path.startsWith("http")) {
                        return path;
                    } else {
                        return Path.of(path).getFileName().toString();
                    }
                })
                .sorted()
                .collect(Collectors.toList());

        context.setFiles(fileNames);
        context.setDateFrom(dateFrom);
        context.setDateTo(dateTo);

        return context;
    }
}
