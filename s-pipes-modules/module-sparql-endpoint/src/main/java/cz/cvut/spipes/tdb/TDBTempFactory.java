package cz.cvut.spipes.tdb;

import cz.cvut.spipes.config.ExecutionConfig;
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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;

@Slf4j
public class TDBTempFactory {

    private static final ReferenceQueue<Model> referenceQueue = new ReferenceQueue<>();
    private static final Set<TDBModelFinalizer> references = ConcurrentHashMap.newKeySet();

    public static Model createTDBModel() {
        finalizePhantomModels();
        Path tempDir = getTempDir();
        log.debug("Creating temporary TDB dataset at directory {}.", tempDir);
        Dataset ds = TDBFactory.createDataset(tempDir.toString());
        Model outModel = ds.getNamedModel(getRandomModelUri());
        setUpFinalization(outModel);
        return ds.getNamedModel(getRandomModelUri());
    }


    private static String getRandomModelUri() {
        return "http://onto.felk.cvut.cz/model/" + Instant.now() + "--" + new Random().nextInt(10000);
    }

    private static Path getTempDir() {
        try {
            Path tempDir = Files.createTempDirectory(ExecutionConfig.getTempDirectoryPath(), "tdb-");
            return tempDir;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Temporary directory could not be created.");
        }
    }

    private static void setUpFinalization(Model tdbModel) {
        String modelLocation = TDBModelHelper.getLocation(tdbModel);
        log.trace("Scheduling removal of directory {} on JVM exit.", modelLocation);
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
