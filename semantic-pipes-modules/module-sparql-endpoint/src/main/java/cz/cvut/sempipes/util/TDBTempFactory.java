package cz.cvut.sempipes.util;

import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.io.FileUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.store.GraphTDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TDBTempFactory {

    private static final Logger LOG = LoggerFactory.getLogger(TDBTempFactory.class);
    private static final ReferenceQueue<Model> referenceQueue = new ReferenceQueue<>();
    private static final Set<TDBModelFinalizer> references = ConcurrentHashMap.newKeySet();

    public static Model createTDBModel() {
        Path tempDir = getTempDir();
        LOG.debug("Creating temporary TDB dataset at directory {}.", tempDir);
        Dataset ds = TDBFactory.createDataset(tempDir.toString());
        Model outModel = ds.getNamedModel(getRandomModelUri());
        finalizePhantomModels();
        setUpFinalization(outModel);
        return ds.getNamedModel(getRandomModelUri());
    }


    private static String getRandomModelUri() {
        return "http://onto.felk.cvut.cz/model/" + Instant.now() + "--" + new Random().nextInt(10000);
    }

    private static Path getTempDir() {
        try {
            Path tempDir = Files.createTempDirectory("tdb-");
            return tempDir;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Temporary directory could not be created.");
        }
    }

    private static void setUpFinalization(Model tdbModel) {
        String modelLocation = TDBModelHelper.getLocation(tdbModel);
        LOG.trace("Scheduling removal of directory {} on JVM exit.", modelLocation);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtils.deleteQuietly(new File(modelLocation))));
        references.add(new TDBModelFinalizer(tdbModel, referenceQueue));
    }

    private static void finalizePhantomModels() {
        Reference<?> referenceFromQueue;
        while ((referenceFromQueue = referenceQueue.poll()) != null) {
            TDBModelFinalizer f = (TDBModelFinalizer) referenceFromQueue;
            f.finalizeResources();
            references.remove(f);
            referenceFromQueue.clear();
        }

    }
}
