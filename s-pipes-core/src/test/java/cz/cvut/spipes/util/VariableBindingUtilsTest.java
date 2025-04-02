package cz.cvut.spipes.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
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
import java.util.concurrent.Executors;

import static cz.cvut.spipes.util.VariableBindingUtils.extendBindingFromURL;
import static org.junit.jupiter.api.Assertions.assertEquals;

class VariableBindingUtilsTest {

    private String testData = """
            PREFIX : <http://onto.fel.cvut.cz/ontologies/s-pipes/query_solution_1740574722036/>
            PREFIX s-pipes: <http://onto.fel.cvut.cz/ontologies/s-pipes/>
            
            :personId 
               s-pipes:has_bound_variable "personId" ;
               s-pipes:has_bound_value "robert-plant" ;
            .                
            :personName
              s-pipes:has_bound_variable "personName" ;
              s-pipes:has_bound_value "Robert Plant" ;
            .
                            
            <http://onto.fel.cvut.cz/ontologies/s-pipes/query_solution_1740574722036>
              a s-pipes:query_solution ;
              s-pipes:has_binding :personId, :personName .
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
        saveDataToFile(tempFile, testData);

        String filePath = tempFile.getAbsolutePath();
        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1); // removing leading '/' for UNIX file paths
        }

        URL extendingVariablesBindingURL = new URL("file:///" + filePath);

        VariablesBinding targetVariablesBinding = new VariablesBinding();
        extendBindingFromURL(targetVariablesBinding, extendingVariablesBindingURL);

        assertEquals("robert-plant", targetVariablesBinding.getNode("personId").toString());
        assertEquals("Robert Plant", targetVariablesBinding.getNode("personName").toString());
    }

    @Test
    void extendBindingFromURLLoadsDataFromHttpUrlCorrectly() throws Exception {
        URL extendingVariablesBindingURL = new URL("http://localhost:" + server.getAddress().getPort() + "/test.ttl");

        VariablesBinding targetVariablesBinding = new VariablesBinding();
        extendBindingFromURL(targetVariablesBinding, extendingVariablesBindingURL);

        assertEquals("robert-plant", targetVariablesBinding.getNode("personId").toString());
        assertEquals("Robert Plant", targetVariablesBinding.getNode("personName").toString());

    }

    private void saveDataToFile(File tempFile, String testData) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(testData.getBytes());
        }
    }
}