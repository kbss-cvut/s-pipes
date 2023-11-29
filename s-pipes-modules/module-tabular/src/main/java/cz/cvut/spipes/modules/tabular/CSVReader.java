package cz.cvut.spipes.modules.tabular;

import cz.cvut.spipes.constants.CSVW;
import cz.cvut.spipes.exception.ResourceNotUniqueException;
import cz.cvut.spipes.modules.model.Column;
import org.supercsv.io.ICsvListReader;

import java.io.IOException;
import java.util.*;

public class CSVReader implements TabularReader {

    ICsvListReader listReader;

    public CSVReader(ICsvListReader listReader) {
        this.listReader = listReader;
    }

    @Override
    public List<String> getHeader() throws IOException {
        return Arrays.asList((listReader.getHeader(true))); // skip the header (can't be used with CsvListReader);
    }

    @Override
    public ArrayList<Column> getOutputColumns(List<String>header) {
        Set<String> columnNames = new HashSet<>();
        ArrayList<Column> columns = new ArrayList<>(header.size());

        for (String columnTitle : header) {
            String columnName = normalize(columnTitle);
            boolean isDuplicate = !columnNames.add(columnName);

            Column schemaColumn = new Column(columnName, columnTitle);

            columns.add(schemaColumn);
            schemaColumn.setTitle(columnTitle);
            if (isDuplicate) throwNotUniqueException(schemaColumn, columnTitle, columnName);
        }
        return columns;
    }

    private String normalize(String label) {
        return label.trim().replaceAll("[^\\w]", "_");
    }

    private void throwNotUniqueException(Column column, String columnTitle, String columnName) {
        throw new ResourceNotUniqueException(
                String.format("Unable to create value of property %s due to collision. " +
                                "Both column titles '%s' and '%s' are normalized to '%s' " +
                                "and thus would refer to the same property url <%s>.",
                        CSVW.propertyUrl,
                        columnTitle,
                        column.getTitle(),
                        columnName,
                        column.getPropertyUrl()));
    }
}
