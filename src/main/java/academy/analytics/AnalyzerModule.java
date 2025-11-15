package academy.analytics;

import academy.model.AnalysisContext;
import academy.model.ParsedLog;

/**
 * Модуль для анализа логов. <br>
 * Каждый модуль собирает свою статистику из логов и сохраняет результаты.
 */
public interface AnalyzerModule {

    /**
     * Обрабатывает одну запись лога. <br>
     * Вызывается для каждой строки лога, чтобы собрать статистику.
     *
     * @param log обрабатываемая запись лога
     */
    void accept(ParsedLog log);

    /**
     * Сохраняет собранную статистику в контекст. <br>
     * Вызывается после обработки всех логов.
     *
     * @param context контекст куда сохраняются результаты
     */
    void applyToContext(AnalysisContext context);
}
