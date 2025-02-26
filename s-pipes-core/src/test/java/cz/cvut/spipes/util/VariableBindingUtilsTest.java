package cz.cvut.spipes.util;
import cz.cvut.spipes.engine.VariablesBinding;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import static cz.cvut.spipes.util.VariableBindingUtils.extendBindingFromURL;
import static org.junit.jupiter.api.Assertions.*;

class VariableBindingUtilsTest {

    @Test
    void testExtendBindingFromURL() {
        try {
            File tempFile = File.createTempFile("test", ".ttl");
            tempFile.deleteOnExit();

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                String testData = """
                        <http://onto.fel.cvut.cz/ontologies/s-pipes/query_solution_1740574722036/personId>
                                <http://onto.fel.cvut.cz/ontologies/s-pipes/has_bound_value>
                                        "robert-plant" ;
                                <http://onto.fel.cvut.cz/ontologies/s-pipes/has_bound_variable>
                                        "personId" .
                                        
                        <http://onto.fel.cvut.cz/ontologies/s-pipes/query_solution_1740574722036>
                                a       <http://onto.fel.cvut.cz/ontologies/s-pipes/query_solution> ;
                                <http://onto.fel.cvut.cz/ontologies/s-pipes/has_binding>
                                        <http://onto.fel.cvut.cz/ontologies/s-pipes/query_solution_1740574722036/personId>.                                             
                        """;
                fos.write(testData.getBytes());
            }

            URL fileURL = tempFile.toURI().toURL();

            VariablesBinding inputVariablesBinding = new VariablesBinding();
            extendBindingFromURL(inputVariablesBinding, fileURL);

            assertFalse(inputVariablesBinding.isEmpty());
        } catch (IOException e) {
            fail("Test should not throw an exception");
        }
    }
}

