package cz.cvut.spipes.modules.util;

import cz.cvut.spipes.modules.model.Region;
import cz.cvut.spipes.registry.StreamResource;
import cz.cvut.spipes.registry.StringStreamResource;

import java.util.List;

public interface TSVConvertor {

    StringStreamResource convertToTSV(StreamResource streamResource);

    List<Region> getMergedRegions(StreamResource streamResource);

    int getNumberTables(StreamResource streamResource);

    String getTableName(StreamResource streamResource);
}
