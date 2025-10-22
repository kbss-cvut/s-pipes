package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.test.web.client.MockRestServiceServer;
//import org.springframework.test.web.client.RequestMatcher;
//import org.springframework.web.client.RestTemplate;
//import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
//import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
//import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class ImportFileFromURLModuleTest {


    static final String SAMPLE_FILE_RESOURCE = "/module/import-file-from-url/sample-file.txt";

//    private RestTemplate restTemplate;
//
//    private MockRestServiceServer mockServer;

//    @BeforeEach
//    public void setUp() {
////        restTemplate = new RestTemplate();
////        this.mockServer = MockRestServiceServer.createServer(restTemplate);
//    }
//        mockServer
//                .expect(requestTo(URL))
//                .andExpect(method(HttpMethod.GET))
//                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
//        mockServer.verify();
    //@Disabled //TODO mockServer works most likely only through Spring API not with URL.connection
    @Test
    public void executeWithTargetFilePath() throws Exception {

        ImportFileFromURLModule m = createModuleWithSampleUrl();

        Path tempFile = Files.createTempFile("import-file-from-url", ".txt");
        m.setTargetFilePath(tempFile);

        m.executeSelf();

        String sampleFileContent = getFileContent(getSampleFilePath());
        String importedFileContent = getFileContent(tempFile);

        assertEquals(sampleFileContent, importedFileContent);
    }

    @Test
    public void executeWithoutTargetFilePath() throws URISyntaxException, IOException {
        ImportFileFromURLModule m = createModuleWithSampleUrl();
        m.setTargetResourceVariable("importedFilePath");

        ExecutionContext ec = m.executeSelf();

        String importedFilePath = ec.getVariablesBinding().getNode("importedFilePath").asLiteral().toString();

        String sampleFileContent = getFileContent(getSampleFilePath());
        String importedFileContent = getFileContent(Paths.get(importedFilePath));

        assertEquals(sampleFileContent, importedFileContent);
    }

    @Test
    public void executeWithUnreachableUrl() throws MalformedURLException {
        ImportFileFromURLModule m = new ImportFileFromURLModule();
        m.setUrl(new URL("http://xxx-unreachable-xxx.cz"));
        m.setInputContext(ExecutionContextFactory.createEmptyContext());

        //TODO specific exception
        assertThrows(RuntimeException.class, m::executeSelf);
    }

    private ImportFileFromURLModule createModuleWithSampleUrl() {
        ImportFileFromURLModule m = new ImportFileFromURLModule();
        m.setUrl(getSampleFileUrl());
        m.setInputContext(ExecutionContextFactory.createEmptyContext());
        return m;
    }

    private Path getSampleFilePath() throws URISyntaxException {
        return Paths.get(getSampleFileUrl().toURI());
    }

    private URL getSampleFileUrl() {
        return getClass().getResource(SAMPLE_FILE_RESOURCE);
    }

    private String getFileContent(Path file) throws IOException {
        return Files.readString(file);
    }

}