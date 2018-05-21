package cz.cvut.sempipes.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Random;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TDBTempFactory {

    private static final Logger LOG = LoggerFactory.getLogger(TDBTempFactory.class);

    public static Model createTDBModel() {
        Path tempDir = getTempDir();
        LOG.trace("Creating temporary TDB dataset at directory {}.", tempDir);
        Dataset ds = TDBFactory.createDataset(tempDir.toString());
        return ds.getNamedModel(getRandomModelUri());
    }


    private static String getRandomModelUri() {
        return "http://onto.felk.cvut.cz/model/" + Instant.now() + "--"+ new Random().nextInt(10000);
    }

    private static Path getTempDir() {
        try {
            Path tempDir = Files.createTempDirectory("junit-tdb-");
            System.out.println("Using temporary TDB at " + tempDir);
            return tempDir;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Temporary directory could not be created.");
        }
    }
}
