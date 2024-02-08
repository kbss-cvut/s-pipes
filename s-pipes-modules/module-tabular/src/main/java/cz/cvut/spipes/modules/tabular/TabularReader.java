package cz.cvut.spipes.modules.tabular;

import cz.cvut.spipes.modules.Mode;
import cz.cvut.spipes.modules.model.Column;
import cz.cvut.spipes.modules.model.Row;
import cz.cvut.spipes.modules.model.TableSchema;
import org.apache.jena.rdf.model.Statement;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface TabularReader {
    List<String> getHeader() throws IOException;
    List<Column> getOutputColumns(List<String>header);
    List<Statement> getRowStatements(List<String>header, List<Column>outputColumns, TableSchema tableSchema) throws IOException;
}
