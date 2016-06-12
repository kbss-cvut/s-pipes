package cz.cvut.sempipes.web;

import cz.cvut.sempipes.config.WebAppConfig;
import cz.cvut.sempipes.constants.SML;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.engine.VariablesBinding;
import cz.cvut.sempipes.modules.ApplyConstructModule;
import cz.cvut.sempipes.service.SempipesServiceController;
import cz.cvut.sempipes.util.RDFMimeType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Assert;
import org.junit.Before;
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

    @Test
    public void testRunNoModule() throws Exception {
        final RequestBuilder rb = get("/module");
        mockMvc.perform(rb).andExpect(status().is4xxClientError());
    }

    @Test
    public void testRunNonExistingModule() throws Exception {
        final RequestBuilder rb = get("/module").
                param(SempipesServiceController.P_ID,"http://onto.fel.cvut.cz/ontologies/eccairs/aviation-3.4.0.2/sempipes");
        mockMvc.perform(rb).andExpect(status().is4xxClientError());
    }

    @Test
    public void testRunExistingModule() throws Exception {
        // TODO identity transformer resolution
        final RequestBuilder rb = post("/module").
                param(SempipesServiceController.P_ID,"http://onto.fel.cvut.cz/ontologies/sempipes/identity-transformer").
                param(SempipesServiceController.P_CONFIG_URL,getClass().getResource("/module-identity/config.ttl").toString()).
                param("paramString","haha").
                param("paramInt","7").
                param("paramIRI","http://test.me").
                accept(RDFMimeType.LD_JSON_STRING).
                contentType(RDFMimeType.N_TRIPLES_STRING).
                content("<http://a> <http://a> <http://a> .");
        MvcResult result = mockMvc.perform(rb)
//                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()).andReturn();
        System.out.println("Resulting JSON: " + result.getResponse().getContentAsString());
    }

//    @Test
//    public void testAcceptJSONLD() throws Exception {
//        testMimeType(RDFMimeType.LD_JSON_STRING, true);
//    }
//    @Test
//    public void testAcceptRDFXML() throws Exception {
//        testMimeType(RDFMimeType.RDF_XML_STRING, true);
//    }

    private void testMimeType( final String mimeType, boolean pass ) throws Exception {
        final RequestBuilder rb = post("/module").
                param(SempipesServiceController.P_ID,"http://onto.fel.cvut.cz/ontologies/sempipes/identity-transformer").
                accept(mimeType);
        mockMvc.perform(rb)
                .andExpect(pass ? status().isOk() : status().is(415)).andReturn();
    }

    @Test
    public void testRunApplyConstructNotReplace() throws Exception {
        File outputBindingFile = File.createTempFile("sempipes-test-output-binding","ttl");
        final StringWriter w = new StringWriter();
        createSimpleModel().write(w,"TURTLE");

        final MockHttpServletRequestBuilder rb = post("/module").
                param(SempipesServiceController.P_ID, "http://onto.fel.cvut.cz/ontologies/test/apply-construct#CreateSampleTriples").
                param(SempipesServiceController.P_CONFIG_URL, getClass().getResource("/module-apply-construct/config.ttl").toString()).
                param(SempipesServiceController.P_OUTPUT_BINDING_URL,outputBindingFile.toURI().toURL().toString().toString()).
                accept(RDFMimeType.LD_JSON_STRING).
                contentType(RDFMimeType.TURTLE_STRING).
                content(w.getBuffer().toString());

        MvcResult result = mockMvc.perform(rb)
//                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()).andReturn();
        System.out.println("=== No replace parameter");
        System.out.println("Resulting JSON: " + result.getResponse().getContentAsString());
        VariablesBinding outputBinding = new VariablesBinding();
        outputBinding.load(new FileInputStream(outputBindingFile), "TURTLE");
        System.out.println("Output binding:" + outputBinding);

        final StringReader jsonLDResult = new StringReader(result.getResponse().getContentAsString());
        final Model mOutput = ModelFactory.createDefaultModel();
        RDFDataMgr.read(mOutput,jsonLDResult,"", Lang.JSONLD);
        Assert.assertEquals(mOutput.listStatements().toList().size(), 2);
    }

    @Test
    public void testRunApplyConstructQueryWithVariable() throws Exception {
        final StringWriter inputData = new StringWriter();
        createSimpleModel().write(inputData,"TURTLE");

        File inputBindingFile = File.createTempFile("sempipes-test-input-binding","ttl");
        VariablesBinding inputVariablesBinding = new VariablesBinding(
                "sampleServiceUri",
                ResourceFactory.createResource("http://martin.inbas.cz/openrdf-sesame/repositories/form-generator?default-graph-uri=http://www.inbas.cz/ontologies/reporting-tool/formGen-977414103")
        );
        inputVariablesBinding.save(new FileOutputStream(inputBindingFile),"TURTLE");

        File outputBindingFile = File.createTempFile("sempipes-test-output-binding","ttl");

        final MockHttpServletRequestBuilder rb = post("/module").
                param(SempipesServiceController.P_ID, "http://onto.fel.cvut.cz/ontologies/test/apply-construct#CreateSampleTriples").
                param(SempipesServiceController.P_CONFIG_URL, getClass().getResource("/module-apply-construct/remote-query.ttl").toString()).
                param(SempipesServiceController.P_OUTPUT_BINDING_URL,outputBindingFile.toURI().toURL().toString().toString()).
                param(SempipesServiceController.P_INPUT_BINDING_URL,inputBindingFile.toURI().toURL().toString().toString()).
                accept(RDFMimeType.LD_JSON_STRING).
                contentType(RDFMimeType.TURTLE_STRING).
                content(inputData.getBuffer().toString());

        MvcResult result = mockMvc.perform(rb)
//                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk()).andReturn();

        System.out.println("=== No replace parameter");
        System.out.println("Resulting JSON: " + result.getResponse().getContentAsString());
        VariablesBinding outputBinding = new VariablesBinding();
        outputBinding.load(new FileInputStream(outputBindingFile), "TURTLE");
        System.out.println("Output binding:" + outputBinding);

        final StringReader jsonLDResult = new StringReader(result.getResponse().getContentAsString());
        final Model mOutput = ModelFactory.createDefaultModel();
        RDFDataMgr.read(mOutput,jsonLDResult,"", Lang.JSONLD);
        Assert.assertEquals(mOutput.listStatements().toList().size(), 54);
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

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
    }
}