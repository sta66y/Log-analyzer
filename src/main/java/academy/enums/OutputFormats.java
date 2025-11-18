package academy.enums;

/** Поддерживаемые форматы вывода результатов анализа. */
public enum OutputFormats {
    JSON("json"),
    MD("markdown"),
    ADOC("adoc");

    private final String id;

    OutputFormats(String id) {
        this.id = id;
    }
    /**
     * Преобразует строковый идентификатор в enum.
     *
     * @param id строковый идентификатор формата
     * @return соответствующий формат вывода
     * @throws IllegalArgumentException если идентификатор не поддерживается
     */
    public static OutputFormats fromId(String id) throws IllegalAccessException {
        for (OutputFormats format : OutputFormats.values()) {
            if (format.id.equals(id)) return format;
        }
        throw new IllegalAccessException("Некорректный формат");
    }
}
