package academy.enums;

public enum OutputFormats {
    JSON("json"),
    MD("markdown"),
    ADOC("adoc");

    private final String id;

    OutputFormats(String id) {
        this.id = id;
    }

    public static OutputFormats fromId(String id) throws IllegalAccessException {
        for(OutputFormats format : OutputFormats.values()) {
            if (format.id.equals(id)) return format;
        }
        throw new IllegalAccessException("Некорректный формат");
    }

}
