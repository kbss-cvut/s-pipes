package cz.cvut.spipes.modules.util;

import cz.cvut.spipes.modules.ResourceFormat;
import cz.cvut.spipes.modules.model.Region;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface FileReaderAdapter {
    void initialise(InputStream inputStream, ResourceFormat sourceResourceFormat, int tableIndex) throws IOException;
    String[] getHeader() throws IOException;
    boolean hasNext() throws IOException;
    List<String> getNextRow() throws IOException;
    List<Region> getMergedRegions();
    String getLabel() throws IOException;
}
