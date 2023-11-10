package cz.cvut.spipes.modules.tabular;

import java.io.IOException;
import java.util.List;

public interface TabularReader {
    List<String> getHeader() throws IOException;
}
