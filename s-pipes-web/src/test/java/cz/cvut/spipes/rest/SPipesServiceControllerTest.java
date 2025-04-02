package cz.cvut.spipes.rest;

import cz.cvut.spipes.config.WebAppConfig;
import cz.cvut.spipes.engine.VariablesBinding;
import cz.cvut.spipes.rest.util.ContextLoaderHelper;
import cz.cvut.spipes.rest.util.ReservedParams;
import cz.cvut.spipes.util.RDFMimeType;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = WebAppConfig.class)
public class SPipesServiceControllerTest {


    public static final String SAMPLE_IDENTITY_MODULE = "http://onto.fel.cvut.cz/ontologies/s-pipes/identity#SampleIdentity";
    public static final String CREATE_SAMPLE_TRIPLES = "http://onto.fel.cvut.cz/ontologies/test/apply-construct#CreateSampleTriples";

    @Autowired
    protected WebApplicationContext ctx;

    private MockMvc mockMvc;
    private MockedStatic<ContextLoaderHelper> mocked;

    @BeforeEach
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
        mocked = mockStatic(ContextLoaderHelper.class);
        mocked.when(ContextLoaderHelper::isKeepUpdated).thenReturn(false);
    }

    @AfterEach
    public void tearDown() {
        if (mocked != null) {
            mocked.close();
        }
    }

    @Test
    public void testRunNoModule() throws Exception {
        final RequestBuilder rb = get("/module");
        mockMvc.perform(rb).andExpect(status().is4xxClientError());
    }

    @Test
    public void testRunNonExistingModule() throws Exception {
        final RequestBuilder rb = get("/module").
                param(ReservedParams.P_ID,"http://example.org/s-pipes/test-no-module");
        mockMvc.perform(rb).andExpect(status().is4xxClientError());
    }

    @Test
    public void testRunInvalidConfigurationModule() throws Exception {
        final RequestBuilder rb = get("/module").
                param(ReservedParams.P_ID,SAMPLE_IDENTITY_MODULE).
                param(ReservedParams.P_CONFIG_URL,"http://example.org/s-pipes/test-no-module/no-configuration");
        mockMvc.perform(rb).andExpect(status().is4xxClientError());
    }

    private MockHttpServletRequestBuilder createDefaultIdentityModuleBuilder() throws Exception {
        return post("/module").
                param(ReservedParams.P_ID, SAMPLE_IDENTITY_MODULE).
                param(ReservedParams.P_CONFIG_URL,getClass().getResource("/module-identity/config.ttl").toURI().toURL().toString()).
                param("paramString","haha").
                param("paramInt","7").
                param("paramIRI","http://test.me").
                contentType(RDFMimeType.N_TRIPLES_STRING).
                content("<http://a/b> <http://a/b> <http://a/b> .");
    }

    @Test
    public void testRunExistingModule() throws Exception {
        MvcResult result = mockMvc.perform(createDefaultIdentityModuleBuilder().
                accept(RDFMimeType.LD_JSON_STRING)
        ).andExpect(status().isOk()).andReturn();
        log.info("Resulting JSON: " + result.getResponse().getContentAsString());
    }

    @Test
    public void testAcceptRDFMimeTypes() throws Exception {
        testMimeType(RDFMimeType.N_TRIPLES_STRING, true);
        testMimeType(RDFMimeType.TURTLE_STRING, true);
        testMimeType(RDFMimeType.LD_JSON_STRING, true);
        testMimeType(RDFMimeType.RDF_XML_STRING, true);
    }

    private void testMimeType( final String mimeType, boolean pass ) throws Exception {
        MvcResult result = mockMvc.perform(createDefaultIdentityModuleBuilder().
                accept(mimeType)
        ).andExpect(pass ? status().isOk() : status().is4xxClientError()).andReturn();
        log.info("Result: {}", result.getResponse().getContentAsString());
        final Model m = ModelFactory.createDefaultModel();
        try {
            m.read(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()), "", RDFLanguages.contentTypeToLang(mimeType).getName());
        } catch(Exception e) {
            fail("Could not parse the result back. Reason: " + e.getMessage());
        }
    }

    private Model createSimpleModel() {
        Model model = ModelFactory.createDefaultModel();
        model.add(
                model.getResource("http://example.org"),
                RDFS.label,
                ResourceFactory.createPlainLiteral("illustration")
        );
        return model;
    }

    private void testModule(
            String id,
            String resourceConfig,
            Model inputModel,
            VariablesBinding inputVariablesBinding,
            VariablesBinding expectedOutputVariablesBinding,
            int expectedNumberOfStatements

    ) throws Exception {
        log.info("Testing module with parameters:\n - id={},\n - cfg={},\n - inputBinding={}", id, resourceConfig,inputVariablesBinding);
        if ( inputVariablesBinding == null ) {
            inputVariablesBinding = new VariablesBinding();
        }
        File inputBindingFile = File.createTempFile("s-pipes-test-input-binding","ttl");
        inputVariablesBinding.save(new FileOutputStream(inputBindingFile), Lang.TTL);

        if ( expectedOutputVariablesBinding == null ) {
            expectedOutputVariablesBinding = new VariablesBinding();
        }
        File outputBindingFile = File.createTempFile("s-pipes-test-output-binding","ttl");
        final StringWriter w = new StringWriter();
        inputModel.write(w,"TURTLE");

        MockHttpServletRequestBuilder rb = inputModel.isEmpty() ? get("/module") : post("/module").contentType(RDFMimeType.TURTLE_STRING).
                content(w.getBuffer().toString());
        rb = rb.param(ReservedParams.P_ID, id).
                param(ReservedParams.P_CONFIG_URL, getClass().getResource(resourceConfig).toString()).
                param(ReservedParams.P_INPUT_BINDING_URL,inputBindingFile.toURI().toURL().toString()).
                param(ReservedParams.P_OUTPUT_BINDING_URL,outputBindingFile.toURI().toURL().toString()).
                accept(RDFMimeType.LD_JSON_STRING);

        MvcResult result = mockMvc.perform(rb)
//                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()).andReturn();
        VariablesBinding outputBinding = new VariablesBinding();
        outputBinding.load(new FileInputStream(outputBindingFile), "TURTLE");
//        Assert.assertEquals(Lists.newArrayList(outputBinding.getVarNames()), Lists.newArrayList(expectedOutputVariablesBinding.getVarNames())); TODO uncomment !!!

        log.info(" - content returned: {}", result.getResponse().getContentAsString());

        final StringReader res = new StringReader(result.getResponse().getContentAsString());
        final Model mOutput = ModelFactory.createDefaultModel();
        RDFDataMgr.read(mOutput,res,"", Lang.JSONLD);
        assertEquals(expectedNumberOfStatements, mOutput.listStatements().toList().size());
    }

    @Test
    public void testRunApplyConstructNotReplace() throws Exception {
        testModule(
                CREATE_SAMPLE_TRIPLES,
                "/module-apply-construct/config.ttl",
                createSimpleModel(),
                null,
                null,
                2);
    }

    @Test
    public void testProcessServicePostRequestGoesThroughWithFiles() throws Exception {
        MockMultipartFile testFile1
                = new MockMultipartFile(
                "testFile1",
                "testFile1.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Test content 1".getBytes()
        );
        MockMultipartFile testFile2
                = new MockMultipartFile(
                "testFile2",
                "testFile2.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Test content 2".getBytes()
        );

        MockHttpServletRequestBuilder rb = multipart("/service")
                .file(testFile1)
                .file(testFile2)
                .param("testKey1", "@testFile1.txt")
                .param("testKey2", "@testFile2.txt")
                .accept(RDFMimeType.LD_JSON_STRING);

        mockMvc.perform(rb)
                .andExpect(status().isBadRequest());
    }

    @Disabled // works only within fell vpn
    @Test
    public void testRunApplyConstructQueryWithVariable() throws Exception {
        VariablesBinding inputVariablesBinding = new VariablesBinding(
                "sampleServiceUri",
                ResourceFactory.createResource("http://martin.inbas.cz/rdf4j-server/repositories/form-generator?default-graph-uri=http://www.inbas.cz/ontologies/reporting-tool/formGen-307795792")
        );

        testModule(
                CREATE_SAMPLE_TRIPLES,
                "/module-apply-construct/remote-query.ttl",
                createSimpleModel(),
                inputVariablesBinding,
                inputVariablesBinding,
                0);
        // TODO check number based on service logic
    }

    @Disabled // works only within fel vpn
    @Test
    public void testByReportingTool() throws Exception {
        VariablesBinding inputVariablesBinding = new VariablesBinding();
        inputVariablesBinding.add(
                "repositoryUrl",
                ResourceFactory.createResource("http://martin.inbas.cz/openrdf-sesame/repositories/form-generator")
        );
        inputVariablesBinding.add(
                "graphId",
                ResourceFactory.createResource("http://www.inbas.cz/ontologies/reporting-tool/formGen1647127699")
        );
        inputVariablesBinding.add(
                "eventType",
                ResourceFactory.createResource("http://onto.fel.cvut.cz/ontologies/eccairs/aviation-3.4.0.2/vl-a-390/v-2200101")
        );
        inputVariablesBinding.add(
                "event",
                ResourceFactory.createResource("http://onto.fel.cvut.cz/ontologies/ufo/Event#instance1610141053")
        );

        testModule(
                "http://onto.fel.cvut.cz/ontologies/aviation/eccairs-form-generation-0.2/generateEccairsForms_Return",
                "/module-generate-eccairs-forms/config.ttl",
                ModelFactory.createDefaultModel(),
                inputVariablesBinding,
                null,
                0);
        // TODO check number based on service logic
    }
}