package cz.cvut.spipes.engine;

import org.apache.jena.ontology.OntDocumentManager;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecutionContextImplTest {

    @Test
    void testGetScriptFiles(){
        List<String> requiredResources = Stream.of("/pipeline/config.ttl", "/sample/sample.ttl").toList();
        List<String> localPaths = requiredResources.stream().map(r -> r.replaceFirst("/[^/]+/", "")).toList();
        List<File> requiredFiles = requiredResources.stream()
                .map(this::resourcePathAsFile)
                .toList();
        List<String> requiredFilesStr = requiredFiles.stream().map(f -> f.toString().replaceAll("\\\\", "/")).toList();
        List<String> uris = Arrays.asList(
                "http://onto.fel.cvut.cz/ontologies/s-pipes/test/pipeline-config",
                "http://onto.fel.cvut.cz/ontologies/test/sample/config"
        );

        List<File> missingRequiredFiles = requiredFiles.stream()
                .filter(f -> !f.exists())
                .toList();

        assertTrue(
                requiredFiles.size() == requiredFiles.size(),
                () -> "Missing test required resource [%s]".formatted(
                        missingRequiredFiles.stream()
                                .map(f -> "\"%s\"".formatted(f.getAbsolutePath()))
                                .collect(Collectors.joining(", "))
                )
        );

        // set up the location map
        for(int i = 0; i < requiredFiles.size(); i++) {
            OntDocumentManager.getInstance().addAltEntry(uris.get(i), requiredFiles.get(i).toString());
        }

        List<String> scriptPaths = Stream.of("/pipeline", "/sample")
                .map(s -> resourcePathAsFile(s).toString())
                .toList();


//        http://onto.fel.cvut.cz/ontologies/s-pipes/test/pipeline-config, /pipeline/config.ttl
//        http://onto.fel.cvut.cz/ontologies/test/sample/config, /sample/sample.ttl

        test(uris.get(0), scriptPaths, 1); // ontology iri
        test(uris.get(1), scriptPaths, 1); // ontology iri
        test(requiredFilesStr.get(0), scriptPaths, 1); // absolute path
        test(requiredFilesStr.get(1), scriptPaths, 1); // absolute path
        test("file:/" + requiredFilesStr.get(0), scriptPaths, 1); // absolute path
        test("file:/" + requiredFilesStr.get(1), scriptPaths, 1); // absolute path
        test("file:///" + requiredFilesStr.get(0), scriptPaths, 1); // absolute path
        test("file:///" + requiredFilesStr.get(1), scriptPaths, 1); // absolute path
        test(localPaths.get(0), scriptPaths, 1); // relative path
        test(localPaths.get(1), scriptPaths, 1); // relative path
        test("./" + localPaths.get(0), scriptPaths, 1); // relative path
        test("./" + localPaths.get(1), scriptPaths, 1); // relative path
        test("../pipeline/" + localPaths.get(0), scriptPaths, 1); // relative path
        test("../sample/" + localPaths.get(1), scriptPaths, 1); // relative path
    }

    protected File resourcePathAsFile(String resourcePath){
        return new File(URI.create(getClass().getResource(resourcePath).toString()));
    }

    protected void test(String uri, List<String> scriptPaths, int expectedSize){
        List<File> ret = ExecutionContextImpl.getScriptFiles(uri, scriptPaths);
        assertEquals(expectedSize, ret.size(),
                "Did not found %d expected number of files for uri <%s>"
                        .formatted(expectedSize, uri)
        );
    }
}