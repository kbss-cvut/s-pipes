package cz.cvut.sempipes.web;

import cz.cvut.sempipes.config.WebAppConfig;
import cz.cvut.sempipes.engine.VariablesBinding;
import cz.cvut.sempipes.service.SempipesServiceController;
import cz.cvut.sempipes.util.RDFMimeType;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
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
                param(SempipesServiceController.P_ID,"http://onto.fel.cvut.cz/ontologies/sempipes/identity-transformer").
                param(SempipesServiceController.P_CONFIG_URL,"http://example.org/sempipes/test-no-module/no-configuration");
        mockMvc.perform(rb).andExpect(status().is4xxClientError());
    }

    private MockHttpServletRequestBuilder createDefaultIdentityModuleBuilder() throws Exception {
        // TODO identity transformer resolution
        return post("/module").
                param(SempipesServiceController.P_ID,"http://onto.fel.cvut.cz/ontologies/sempipes/identity-transformer").
                param(SempipesServiceController.P_CONFIG_URL,getClass().getResource("/module-identity/config.ttl").toURI().toURL().toString()).
                param("paramString","haha").
                param("paramInt","7").
                param("paramIRI","http://test.me").
                contentType(RDFMimeType.N_TRIPLES_STRING).
                content("<http://a/b> <http://a/b> <http://a/b> .");
    }

    @Ignore
    @Test
    public void testRunExistingModule() throws Exception {
        MvcResult result = mockMvc.perform(createDefaultIdentityModuleBuilder().
                accept(RDFMimeType.LD_JSON_STRING)
        ).andExpect(status().isOk()).andReturn();
        System.out.println("Resulting JSON: " + result.getResponse().getContentAsString());
    }

    @Ignore
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
        System.out.println("Result: " + result.getResponse().getContentAsString());
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

        final MockHttpServletRequestBuilder rb = post("/module").
                param(SempipesServiceController.P_ID, id).
                param(SempipesServiceController.P_CONFIG_URL, getClass().getResource(resourceConfig).toString()).
                param(SempipesServiceController.P_INPUT_BINDING_URL,inputBindingFile.toURI().toURL().toString()).
                param(SempipesServiceController.P_OUTPUT_BINDING_URL,outputBindingFile.toURI().toURL().toString()).
                accept(RDFMimeType.LD_JSON_STRING).
                contentType(RDFMimeType.TURTLE_STRING).
                content(w.getBuffer().toString());

        MvcResult result = mockMvc.perform(rb)
//                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()).andReturn();
        System.out.println("Result: " + result.getResponse().getContentAsString());
        VariablesBinding outputBinding = new VariablesBinding();
        outputBinding.load(new FileInputStream(outputBindingFile), "TURTLE");
        Assert.assertEquals(Lists.newArrayList(outputBinding.getVarNames()), Lists.newArrayList(expectedOutputVariablesBinding.getVarNames()));

        final StringReader res = new StringReader(result.getResponse().getContentAsString());
        final Model mOutput = ModelFactory.createDefaultModel();
        RDFDataMgr.read(mOutput,res,"", Lang.JSONLD);
        Assert.assertEquals(mOutput.listStatements().toList().size(), expectedNumberOfStatements);
    }

    @Ignore
    @Test
    public void testRunApplyConstructNotReplace() throws Exception {
        testModule(
                "http://onto.fel.cvut.cz/ontologies/test/apply-construct#CreateSampleTriples",
                "/module-apply-construct/config.ttl",
                createSimpleModel(),
                null,
                null,
                2);
    }

    @Ignore
    @Test
    public void testRunApplyConstructQueryWithVariable() throws Exception {
        VariablesBinding inputVariablesBinding = new VariablesBinding(
                "sampleServiceUri",
                ResourceFactory.createResource("http://martin.inbas.cz/openrdf-sesame/repositories/form-generator?default-graph-uri=http://www.inbas.cz/ontologies/reporting-tool/formGen-977414103")
        );

        testModule(
                "http://onto.fel.cvut.cz/ontologies/test/apply-construct#CreateSampleTriples",
                "/module-apply-construct/remote-query.ttl",
                createSimpleModel(),
                inputVariablesBinding,
                null,
                54);
    }
 }