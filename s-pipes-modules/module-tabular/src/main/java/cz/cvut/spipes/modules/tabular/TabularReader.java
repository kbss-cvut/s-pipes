package cz.cvut.spipes.modules.tabular;

import cz.cvut.spipes.modules.Mode;
import cz.cvut.spipes.modules.model.Column;
import cz.cvut.spipes.modules.model.Row;
import cz.cvut.spipes.modules.model.TableSchema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface TabularReader {
    List<String> getHeader() throws IOException;
    ArrayList<Column> getOutputColumns(List<String>header);
    Set<Row> getRows(TableSchema tableSchema, String sourceResourceUri, Mode outputMode) throws IOException;
}
