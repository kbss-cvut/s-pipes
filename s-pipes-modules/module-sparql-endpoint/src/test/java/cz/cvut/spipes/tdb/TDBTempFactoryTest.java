package cz.cvut.spipes.tdb;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.store.GraphTDB;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TDBTempFactoryTest {

    @Disabled//doesn't work on windows
    @Test
    public void createTDBModelRemovesDirectoryForPreviousModelsWithoutReferences() throws InterruptedException {
        String inputModelPath = TDBTempFactoryTest.class.getResource("/small-model.ttl").getFile().toString();

        List<Path> locations = new LinkedList<>();

        for (int i = 0; i<10; i++) {
            Model model = TDBTempFactory.createTDBModel().read(inputModelPath);
            Path loc = getLocation(model);
            locations.add(loc);
            assertTrue(Files.isDirectory(loc));
            System.gc();
            TimeUnit.SECONDS.sleep(1);
        }

        Model model = TDBTempFactory.createTDBModel().read(inputModelPath);
        FileUtils.deleteQuietly(getLocation(model).toFile());

        List<Path> existingDirs = locations.stream()
            .filter(loc -> Files.exists(loc)).collect(Collectors.toList());

        assertEquals(existingDirs, new LinkedList<>());
    }

    private Path getLocation(Model tdbModel) {
        return Paths.get(((GraphTDB) tdbModel.getGraph()).getDSG().getLocation().getDirectoryPath());
    }
}