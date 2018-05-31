package cz.cvut.sempipes.util;

import java.io.File;
import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TDBModelFinalizer extends PhantomReference<Model> {

    private static final Logger LOG = LoggerFactory.getLogger(TDBModelFinalizer.class);
    private final String datasetLocation;

    public TDBModelFinalizer(Model referent, ReferenceQueue<? super Model> q) {
        super(referent, q);
        this.datasetLocation = TDBModelHelper.getLocation(referent);
    }

    public void finalizeResources() {
        LOG.debug("Removing temporary TDB dataset at directory {}.", datasetLocation);
        try {
            FileUtils.deleteDirectory(new File(datasetLocation));
        } catch (IOException e) {
            LOG.error("Could not remove directory at {}, reason: {}.", datasetLocation, e);
        }
    }
}
