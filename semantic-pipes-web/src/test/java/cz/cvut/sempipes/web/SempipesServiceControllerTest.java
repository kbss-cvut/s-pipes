package cz.cvut.sempipes.web;

import cz.cvut.sempipes.service.RDFMimeType;
import cz.cvut.sempipes.service.SempipesServiceController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@WebAppConfiguration()
public class SempipesServiceControllerTest {

    @Autowired
    protected WebApplicationContext ctx;

    private MockMvc mockMvc;

    @Test
    public void testRunNoModule() throws Exception {
        final RequestBuilder rb = get("/serviceGet");
        mockMvc.perform(rb).andExpect(status().is4xxClientError());
    }

    @Test
    public void testRunNonExistingModule() throws Exception {
        final RequestBuilder rb = get("/serviceGet").param("id","http://onto.fel.cvut.cz/ontologies/eccairs/aviation-3.4.0.2/sempipes");
        mockMvc.perform(rb).andExpect(status().is4xxClientError());
    }

    @Test
    public void testRunExistingModule() throws Exception {
        final RequestBuilder rb = post("/servicePost").
                param("id","cz.cvut.sempipes.modules.IdentityModule").
                param("paramString","haha").
                param("paramInt","7").
                param("paramIRI","http://test.me").
//                accept(RDFMimeType.JSONLD).
//                contentType(RDFMimeType.N_TRIPLES).
                contentType("text/plain").
                content("<http://a> <http://a> <http://a> .");
        mockMvc.perform(rb).andExpect(status().isOk());
    }

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx).build();
    }

    @Configuration
    @EnableWebMvc
    public static class TestConfiguration {

        @Bean
        public SempipesServiceController contactController() {
            return new SempipesServiceController();
        }

    }
}