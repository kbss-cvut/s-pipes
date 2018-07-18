package cz.cvut.sempipes.util;

import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Created by Miroslav Blasko on 12.11.16.
 */
public class JenaUtilsTest {

    private static final String HASH_FILE_PREFIX = "hash-example-";

    @Test
    public void computeHash() throws Exception {

        String[] exampleIds = {"1", "2", "3", "4"};

        Map<String, String> file2ModelHashMap = Arrays.stream(exampleIds)
                .map(id -> "/util/hash-example-" + id + ".ttl")
                .collect(Collectors.toMap(
                        path -> path,
                        path -> {
                            URL fileUrl = JenaUtilsTest.class.getResource(path);
                            Model m = ModelFactory.createDefaultModel();
                            m.read(String.valueOf(fileUrl), null, FileUtils.langTurtle);
                            return JenaUtils.computeHash(m);
                        }
                ));


        Iterator<Map.Entry<String, String>> it = file2ModelHashMap.entrySet().iterator();
        Map.Entry<String, String> firstHashEntry = it.next();
        while (it.hasNext()) {
            Map.Entry<String, String> nextHashEntry = it.next();
            String errMessage = "Hashes of ontologies from files " + firstHashEntry.getKey() + " and " + nextHashEntry.getKey() + " are not same.";
            assertEquals(errMessage, firstHashEntry.getValue(), nextHashEntry.getValue());
        }
    }

}