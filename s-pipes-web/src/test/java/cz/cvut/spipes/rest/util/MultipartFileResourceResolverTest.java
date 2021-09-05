package cz.cvut.spipes.rest.util;

import cz.cvut.spipes.config.WebAppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = WebAppConfig.class)
class MultipartFileResourceResolverTest {

    @Autowired
    private MultipartFileResourceResolver resolver;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testResolveResourcesReplacesFilenamesWithUrisInParameters() {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("testKey1", "@testFile1.txt");
        parameters.add("testKey2", "@testFile2.txt");

        MockMultipartFile testFile1
                = new MockMultipartFile(
                "testFile1",
                "testFile1.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello 1".getBytes()
        );
        MockMultipartFile testFile2
                = new MockMultipartFile(
                "testFile2",
                "testFile2.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello 2".getBytes()
        );

        MultipartFile[] files = new MultipartFile[]{testFile1, testFile2};

        MultiValueMap<String, String> newParameters = resolver.resolveResources(parameters, files);

        assertEquals(parameters.keySet(), newParameters.keySet());
        assertEquals(1, newParameters.get("testKey1").size());
        assertEquals(1, newParameters.get("testKey2").size());
        assertTrue(isValidUri(newParameters.get("testKey1").get(0)));
        assertTrue(isValidUri(newParameters.get("testKey2").get(0)));
    }

    @Test
    void testResolveResourcesDoesNotReplaceWrongFilenames() {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("testKey1", "@WRONG_FILENAME.txt");
        parameters.add("testKey2", "testFile2.txt");

        MockMultipartFile testFile1
                = new MockMultipartFile(
                "testFile1",
                "testFile1.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello 1".getBytes()
        );
        MockMultipartFile testFile2
                = new MockMultipartFile(
                "testFile2",
                "testFile2.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello 2".getBytes()
        );

        MultipartFile[] files = new MultipartFile[]{testFile1, testFile2};

        MultiValueMap<String, String> newParameters = resolver.resolveResources(parameters, files);

        assertEquals(parameters, newParameters);
    }

    private boolean isValidUri(String uriToCheck) {
        try {
            new URI(uriToCheck);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}