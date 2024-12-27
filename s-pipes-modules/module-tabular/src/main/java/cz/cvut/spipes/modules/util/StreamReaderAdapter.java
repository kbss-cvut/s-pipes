package cz.cvut.spipes.modules.util;

import cz.cvut.spipes.modules.ResourceFormat;
import cz.cvut.spipes.modules.TabularModule;
import cz.cvut.spipes.modules.model.Region;
import cz.cvut.spipes.registry.StreamResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

public interface StreamReaderAdapter {
    static final Logger LOG = LoggerFactory.getLogger(TabularModule.class);
    void initialise(InputStream inputStream, ResourceFormat sourceResourceFormat, int tableIndex, boolean acceptInvalidQuoting, Charset inputCharset, StreamResource sourceResource) throws IOException;
    String[] getHeader(Boolean skipHeader) throws IOException;
    boolean hasNextRow() throws IOException;
    List<String> getNextRow() throws IOException;
    List<Region> getMergedRegions();
    String getSheetLabel() throws IOException;
    void close() throws IOException;
}
