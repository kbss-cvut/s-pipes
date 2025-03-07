package cz.cvut.spipes.util;
import cz.cvut.spipes.engine.VariablesBinding;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Executors;

import static cz.cvut.spipes.util.VariableBindingUtils.extendBindingFromURL;
import static org.junit.jupiter.api.Assertions.*;
import static org.reflections.Reflections.log;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

class VariableBindingUtilsTest {

    private String testData = """
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

    private HttpServer server;

    @BeforeEach
    void setup() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/test.ttl", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                exchange.sendResponseHeaders(200, testData.getBytes().length);
                exchange.getResponseBody().write(testData.getBytes());
                exchange.close();
            }
        });
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    @AfterEach
    void teardown() {
        server.stop(0);
    }

    @Test
    void extendBindingFromURLLoadsLocalDataCorrectly(@TempDir File tempDir) throws Exception {
        File tempFile = new File(tempDir, "test.ttl");

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(testData.getBytes());
        }

        // Emulating saving of the path like SaveModelToTemporaryFile does
        // Reading URL from parameters like ServiceParametersHelper does
        String filePath = tempFile.toURI().toURL().toString();
        URL fileURL = new URL(filePath);

        VariablesBinding inputVariablesBinding = new VariablesBinding();
        extendBindingFromURL(inputVariablesBinding, fileURL);

        assertEquals("robert-plant", inputVariablesBinding.getNode("personId").toString());
        assertEquals("Robert Plant", inputVariablesBinding.getNode("personName").toString());
    }

    @Test
    void extendBindingFromURLLoadsDataFromHttpUrlCorrectly() throws Exception {
        URL url = new URL("http://localhost:" + server.getAddress().getPort() + "/test.ttl");

        VariablesBinding inputVariablesBinding = new VariablesBinding();
        extendBindingFromURL(inputVariablesBinding, url);

        assertEquals("robert-plant", inputVariablesBinding.getNode("personId").toString());
        assertEquals("Robert Plant", inputVariablesBinding.getNode("personName").toString());

    }

}