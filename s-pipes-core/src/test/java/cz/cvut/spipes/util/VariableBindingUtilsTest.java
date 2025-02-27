package cz.cvut.spipes.util;
import cz.cvut.spipes.engine.VariablesBinding;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static cz.cvut.spipes.util.VariableBindingUtils.extendBindingFromURL;
import static org.junit.jupiter.api.Assertions.*;
import static org.reflections.Reflections.log;

class VariableBindingUtilsTest {

    @Test
    void extendBindingFromURLLoadsLocalDataCorrectly(@TempDir File tempDir) throws Exception {
        File tempFile = new File(tempDir, "test.ttl");

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            String testData = """
                    <http://onto.fel.cvut.cz/ontologies/s-pipes/query_solution_1740574722036/personId>
                            <http://onto.fel.cvut.cz/ontologies/s-pipes/has_bound_value>
                                    "robert-plant" ;
                            <http://onto.fel.cvut.cz/ontologies/s-pipes/has_bound_variable>
                                    "personId" .
                                    
                    <http://onto.fel.cvut.cz/ontologies/s-pipes/query_solution_1740574722036/personName>
                            <http://onto.fel.cvut.cz/ontologies/s-pipes/has_bound_value>
                                    "Robert Plant" ;
                            <http://onto.fel.cvut.cz/ontologies/s-pipes/has_bound_variable>
                                    "personName" .
                                    
                    <http://onto.fel.cvut.cz/ontologies/s-pipes/query_solution_1740574722036>
                            a       <http://onto.fel.cvut.cz/ontologies/s-pipes/query_solution> ;
                            <http://onto.fel.cvut.cz/ontologies/s-pipes/has_binding>
                                    <http://onto.fel.cvut.cz/ontologies/s-pipes/query_solution_1740574722036/personId>, 
                                    <http://onto.fel.cvut.cz/ontologies/s-pipes/query_solution_1740574722036/personName>.                                             
                    """;
            fos.write(testData.getBytes());
        }

        // Emulating saving of the path like SaveModelToTemporaryFile does
        // Reading URL from parameters like ServiceParametersHelper does
        URL fileURL = new URL("file://" + tempFile.getAbsolutePath());

        // Working way you read URL
        //URL fileURL = tempFile.toURI().toURL();

        VariablesBinding inputVariablesBinding = new VariablesBinding();
        extendBindingFromURL(inputVariablesBinding, fileURL);

        assertEquals("robert-plant", inputVariablesBinding.getNode("personId").toString());
        assertEquals("Robert Plant", inputVariablesBinding.getNode("personName").toString());
    }
}

