package cz.cvut.spipes.modules.tabular;

import cz.cvut.spipes.modules.model.Column;
import cz.cvut.spipes.modules.model.Region;
import cz.cvut.spipes.modules.model.TableSchema;
import org.apache.jena.rdf.model.Statement;

import java.io.IOException;
import java.util.List;

public interface TabularReader {
    List<String> getHeader() throws IOException;
    List<Statement> getRowStatements(List<String>header, List<Column>outputColumns, TableSchema tableSchema) throws IOException;
    int getNumberOfRows();
    String getTableName();
    int getTablesCount();
    List<Region> getMergedRegions();
}
