package academy.util;

import academy.model.ResponseSize;
import java.time.LocalDate;
import java.util.List;

/** Утилитарные методы для форматирования данных в writer'ах. */
public class WriterUtil {

    /**
     * Возвращает читаемое имя HTTP кода ответа.
     *
     * @param code HTTP код ответа
     * @return читаемое имя кода
     */
    public static String getHttpCodeName(int code) {
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

    /**
     * Форматирует значение размера ответа по типу.
     *
     * @param responseSize статистика размеров ответов
     * @param type тип значения (maxValue, averageValue, p95Value)
     * @return отформатированное строковое значение
     * @throws IllegalArgumentException если тип не поддерживается
     */
    public static String formatResponseSize(ResponseSize responseSize, String type) {
        return switch (type.toLowerCase()) {
            case "maxvalue" -> Integer.toString(responseSize.maxValue());
            case "averagevalue" -> Double.toString(responseSize.averageValue());
            case "p95value" -> Integer.toString(responseSize.p95Value());
            default -> throw new IllegalArgumentException("Неизвестный тип размера ответа: '" + type + "'. " +
                "Доступные типы: maxValue, averageValue, p95Value"
            );
        };
    }

    /**
     * Форматирует количество запросов для отображения.
     *
     * @param totalRequestsCount общее количество запросов
     * @return строковое представление числа
     */
    public static String formatTotalRequestsDisplay(int totalRequestsCount) {
        return Integer.toString(totalRequestsCount);
    }

    /**
     * Форматирует дату для отображения.
     *
     * @param date дата для форматирования
     * @return строковое представление даты или "-" если null
     */
    public static String formatDisplayDate(LocalDate date) {
        return date != null ? date.toString() : "-";
    }

    /**
     * Форматирует список файлов для компактного отображения.
     *
     * @param files список путей к файлам
     * @return форматированная строка с файлами
     */
    public static String formatDisplayFiles(List<String> files) {
        if (files.isEmpty()) return "-";
        if (files.size() == 1) return "`" + files.getFirst() + "`";
        if (files.size() == 2) return "`" + files.getFirst() + "`, `" + files.getLast() + "`";
        return "`" + files.getFirst() + "` и еще " + (files.size() - 1) + " файлов";
    }
}
