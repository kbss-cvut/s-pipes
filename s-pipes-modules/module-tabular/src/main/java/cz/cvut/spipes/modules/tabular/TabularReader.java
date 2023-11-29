package cz.cvut.spipes.modules.tabular;

import cz.cvut.spipes.modules.model.Column;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface TabularReader {
    List<String> getHeader() throws IOException;
    ArrayList<Column> getOutputColumns(List<String>header);
}
