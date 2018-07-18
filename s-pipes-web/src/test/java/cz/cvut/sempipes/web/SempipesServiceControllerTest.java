package cz.cvut.sempipes.web;

import cz.cvut.sempipes.config.WebAppConfig;
import cz.cvut.sempipes.engine.VariablesBinding;
import cz.cvut.sempipes.rest.SempipesServiceController;
import cz.cvut.sempipes.util.RDFMimeType;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.vocabulary.RDFS;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = WebAppConfig.class)
public class SempipesServiceControllerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SempipesServiceControllerTest.class);

    public static final String SAMPLE_IDENTITY_MODULE = "http://onto.fel.cvut.cz/ontologies/s-pipes/identity#SampleIdentity";
    public static final String CREATE_SAMPLE_TRIPLES = "http://onto.fel.cvut.cz/ontologies/test/apply-construct#CreateSampleTriples";

    @Autowired
    protected WebApplicationContext ctx;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
    }

    @Test
    public void testRunNoModule() throws Exception {
        final RequestBuilder rb = get("/module");
        mockMvc.perform(rb).andExpect(status().is4xxClientError());
    }

    @Test
    public void testRunNonExistingModule() throws Exception {
        final RequestBuilder rb = get("/module").
                param(SempipesServiceController.P_ID,"http://example.org/sempipes/test-no-module");
        mockMvc.perform(rb).andExpect(status().is4xxClientError());
    }

    @Test
    public void testRunInvalidConfigurationModule() throws Exception {
        final RequestBuilder rb = get("/module").
                param(SempipesServiceController.P_ID,SAMPLE_IDENTITY_MODULE).
                param(SempipesServiceController.P_CONFIG_URL,"http://example.org/sempipes/test-no-module/no-configuration");
        mockMvc.perform(rb).andExpect(status().is4xxClientError());
    }

    private MockHttpServletRequestBuilder createDefaultIdentityModuleBuilder() throws Exception {
        return post("/module").
                param(SempipesServiceController.P_ID, SAMPLE_IDENTITY_MODULE).
                param(SempipesServiceController.P_CONFIG_URL,getClass().getResource("/module-identity/config.ttl").toURI().toURL().toString()).
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
        LOGGER.info("Resulting JSON: " + result.getResponse().getContentAsString());
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
        ).andExpect(pass ? status().isOk() : status().is(415)).andReturn();
        LOGGER.info("Result: {}", result.getResponse().getContentAsString());
        final Model m = ModelFactory.createDefaultModel();
        try {
            m.read(new ByteArrayInputStream(result.getResponse().getContentAsByteArray()), "", RDFLanguages.contentTypeToLang(mimeType).getName());
        } catch(Exception e) {
            Assert.fail("Could not parse the result back. Reason: " + e.getMessage());
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
        LOGGER.info("Testing module with parameters:\n - id={},\n - cfg={},\n - inputBinding={}", id, resourceConfig,inputVariablesBinding);
        if ( inputVariablesBinding == null ) {
            inputVariablesBinding = new VariablesBinding();
        }
        File inputBindingFile = File.createTempFile("sempipes-test-input-binding","ttl");
        inputVariablesBinding.save(new FileOutputStream(inputBindingFile), "TURTLE");

        if ( expectedOutputVariablesBinding == null ) {
            expectedOutputVariablesBinding = new VariablesBinding();
        }
        File outputBindingFile = File.createTempFile("sempipes-test-output-binding","ttl");
        final StringWriter w = new StringWriter();
        inputModel.write(w,"TURTLE");

        MockHttpServletRequestBuilder rb = inputModel.isEmpty() ? get("/module") : post("/module").contentType(RDFMimeType.TURTLE_STRING).
                content(w.getBuffer().toString());
        rb = rb.param(SempipesServiceController.P_ID, id).
                param(SempipesServiceController.P_CONFIG_URL, getClass().getResource(resourceConfig).toString()).
                param(SempipesServiceController.P_INPUT_BINDING_URL,inputBindingFile.toURI().toURL().toString()).
                param(SempipesServiceController.P_OUTPUT_BINDING_URL,outputBindingFile.toURI().toURL().toString()).
                accept(RDFMimeType.LD_JSON_STRING);

        MvcResult result = mockMvc.perform(rb)
//                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()).andReturn();
        VariablesBinding outputBinding = new VariablesBinding();
        outputBinding.load(new FileInputStream(outputBindingFile), "TURTLE");
//        Assert.assertEquals(Lists.newArrayList(outputBinding.getVarNames()), Lists.newArrayList(expectedOutputVariablesBinding.getVarNames())); TODO uncomment !!!

        LOGGER.info(" - content returned: {}", result.getResponse().getContentAsString());

        final StringReader res = new StringReader(result.getResponse().getContentAsString());
        final Model mOutput = ModelFactory.createDefaultModel();
        RDFDataMgr.read(mOutput,res,"", Lang.JSONLD);
        Assert.assertEquals(expectedNumberOfStatements, mOutput.listStatements().toList().size());
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

    @Ignore // works only within fell vpn
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

    @Ignore // works only within fel vpn
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