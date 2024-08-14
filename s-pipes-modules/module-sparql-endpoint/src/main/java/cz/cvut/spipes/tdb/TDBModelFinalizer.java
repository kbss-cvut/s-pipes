package cz.cvut.spipes.tdb;

import java.io.File;
import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;

@Slf4j
public class TDBModelFinalizer extends PhantomReference<Model> {

    private final String datasetLocation;

    public TDBModelFinalizer(Model referent, ReferenceQueue<? super Model> q) {
        super(referent, q);
        this.datasetLocation = TDBModelHelper.getLocation(referent);
    }

    public void finalizeResources() {
        log.debug("Removing temporary TDB dataset at directory {}.", datasetLocation);
        try {
            FileUtils.deleteDirectory(new File(datasetLocation));
        } catch (IOException e) {
            log.error("Could not remove directory at {}, reason: {}.", datasetLocation, e);
        }
    }
}
