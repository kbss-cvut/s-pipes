package cz.cvut.spipes.modules;

import cz.cvut.spipes.modules.exception.ValueNotFoundException;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum ResourceFormat {
    PLAIN("text/plain"),
    CSV("text/csv"),
    TSV("text/tab-separated-values"),
    HTML("text/html"),
    XLS("application/vnd.ms-excel"),
    XLSM("application/vnd.ms-excel.sheet.macroEnabled.12"),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    private final String value;

    public String getValue() {
        return value;
    }

    public static ResourceFormat fromString(String value) {
        return Arrays.stream(ResourceFormat.values())
                .filter(d -> d.getValue().equals(value))
                .findAny().orElseThrow(() -> new ValueNotFoundException(
                        "Value " + value + " not recognized among valid values of this type, i.e. " +
                                Arrays.stream(ResourceFormat.values()).map(ResourceFormat::getValue).collect(Collectors.toList())
                ));
    }

    ResourceFormat(String value) {
        this.value = value;
    }
}
