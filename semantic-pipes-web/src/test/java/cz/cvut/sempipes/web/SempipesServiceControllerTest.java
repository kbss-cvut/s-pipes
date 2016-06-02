package cz.cvut.sempipes.web;

import cz.cvut.sempipes.config.WebAppConfig;
import cz.cvut.sempipes.util.RDFMimeType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

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
        final RequestBuilder rb = get("/service");
        mockMvc.perform(rb).andExpect(status().is4xxClientError());
    }

    @Test
    public void testRunNonExistingModule() throws Exception {
        final RequestBuilder rb = get("/service").param("id","http://onto.fel.cvut.cz/ontologies/eccairs/aviation-3.4.0.2/sempipes");
        mockMvc.perform(rb).andExpect(status().is4xxClientError());
    }

    @Test
    public void testRunExistingModule() throws Exception {
        final RequestBuilder rb = post("/service").
                param("id","http://onto.fel.cvut.cz/ontologies/sempipes/identity-transformer").
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
        final RequestBuilder rb = post("/service").
                param("id","http://onto.fel.cvut.cz/ontologies/sempipes/identity-transformer").
                accept(mimeType);
        mockMvc.perform(rb)
                .andExpect(pass ? status().isOk() : status().is(415)).andReturn();
    }

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
    }
}