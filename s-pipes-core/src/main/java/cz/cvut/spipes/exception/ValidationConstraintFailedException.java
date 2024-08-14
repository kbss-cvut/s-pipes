package cz.cvut.spipes.exception;

import cz.cvut.spipes.modules.Module;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ValidationConstraintFailedException extends SPipesException {

    private final String module;
    private final String errorMessage;
    private final String failedQuery;
    private final List<Map<String, String>> evidences;

    public ValidationConstraintFailedException(@NotNull Module module,
                                               String errorMessage,
                                               String failedQuery,
                                               List<Map<String, String>> evidences) {

        super(createModuleInfo(module));
        this.module = Optional.ofNullable(module.getResource()).map(Resource::toString).orElse("Unknown");
        this.errorMessage = errorMessage;
        this.failedQuery = failedQuery;
        this.evidences = evidences;

    }

    private static String createModuleInfo(@NotNull  Module module) {
        return Optional.ofNullable(module.getResource())
                .map(r -> String.format("Execution of module %s failed. ", r))
                .orElse("Execution of a module with type %s failed." + module.getTypeURI());
    }

    public String getModule() {
        return module;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getFailedQuery() {
        return failedQuery;
    }

    public List<Map<String, String>> getEvidences() {
        return evidences;
    }

    @Override
    public String toString() {
        return "ValidationConstraintFailedException{\n" +
                  "Validation of constraint failed for the constraint: "+ errorMessage + '\n' +
                 "\"Failed validation constraint :" + '\n' + failedQuery + '\n' +
                "Evidences:\n" + getTableString()+
                '}';
    }

    public String getTableString() {
        if (evidences.isEmpty()) {
            return "No data to display.";
        }

        StringBuilder tableBuilder = new StringBuilder();
        Map<String, Integer> columnWidths = determineColumnWidths();
        String format = createFormatString(columnWidths);
        buildHeader(tableBuilder, columnWidths, format);
        buildRows(tableBuilder, format);
        return tableBuilder.toString();
    }

    private Map<String, Integer> determineColumnWidths() {
        Map<String, Integer> columnWidths = new LinkedHashMap<>();
        for (String key : evidences.get(0).keySet()) {
            columnWidths.put(key, key.length());
        }
        for (Map<String, String> row : evidences) {
            for (Map.Entry<String, String> entry : row.entrySet()) {
                int currentWidth = columnWidths.get(entry.getKey());
                int dataWidth = entry.getValue().length();
                columnWidths.put(entry.getKey(), Math.max(currentWidth, dataWidth));
            }
        }

        return columnWidths;
    }

    private String createFormatString(Map<String, Integer> columnWidths) {
        StringBuilder formatBuilder = new StringBuilder("|");
        for (int width : columnWidths.values()) {
            formatBuilder.append(" %-").append(width).append("s |");
        }
        formatBuilder.append("%n");
        return formatBuilder.toString();
    }

    private void buildHeader(StringBuilder tableBuilder, Map<String, Integer> columnWidths, String format) {
        List<String> headers = new ArrayList<>(columnWidths.keySet());
        tableBuilder.append(String.format(format, headers.toArray()));
        buildSeparator(tableBuilder, columnWidths);
    }

    private void buildRows(StringBuilder tableBuilder, String format) {
        for (Map<String, String> row : evidences) {
            List<String> values = new ArrayList<>();
            for (String key : row.keySet()) {
                values.add(row.get(key));
            }
            tableBuilder.append(String.format(format, values.toArray()));
        }
    }

    private void buildSeparator(StringBuilder tableBuilder, Map<String, Integer> columnWidths) {
        StringBuilder separator = new StringBuilder("+");
        for (int width : columnWidths.values()) {
            for (int i = 0; i < width + 2; i++) {
                separator.append("-");
            }
            separator.append("+");
        }
        tableBuilder.append(separator).append(System.lineSeparator());
    }
}

