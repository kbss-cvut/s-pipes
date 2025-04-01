
package cz.cvut.spipes.debug.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.converter.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.converter.yaml.MappingJackson2YamlHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.kbss.jsonld.ConfigParam;
import cz.cvut.kbss.jsonld.JsonLd;
import cz.cvut.kbss.jsonld.jackson.JsonLdModule;
import cz.cvut.kbss.jsonld.jackson.serialization.SerializationConstants;

@Configuration
@EnableWebMvc
public class WebAppConfig implements WebMvcConfigurer {

    @Bean(name = "jsonLdMapper")
    public ObjectMapper jsonLdObjectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        final JsonLdModule module = new JsonLdModule();
        module.configure(ConfigParam.SCAN_PACKAGE, "cz.cvut.spipes");
        module.configure(SerializationConstants.FORM, SerializationConstants.FORM_COMPACT_WITH_CONTEXT);
        objectMapper.registerModule(module);
        return objectMapper;
    }

    @Primary
    @Bean(name = "objectMapper")
    public ObjectMapper objectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        return objectMapper;
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(new ObjectMapper());
        return converter;
    }

    @Bean
    public HttpMessageConverter<?> createJsonLdMessageConverter() {
        final MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(
                jsonLdObjectMapper());
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.valueOf(JsonLd.MEDIA_TYPE)));
        return converter;
    }

    @Bean
    public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
        final RequestMappingHandlerAdapter adapter = new RequestMappingHandlerAdapter();
        List<HttpMessageConverter<?>> defaultConverters = new ArrayList<>();
        defaultConverters.add(new ByteArrayHttpMessageConverter());
        defaultConverters.add(new StringHttpMessageConverter());
        defaultConverters.add(new ResourceHttpMessageConverter());
        defaultConverters.add(new ResourceRegionHttpMessageConverter());
        defaultConverters.add(new AllEncompassingFormHttpMessageConverter());
        defaultConverters.add(new Jaxb2RootElementHttpMessageConverter());
        defaultConverters.add(new MappingJackson2YamlHttpMessageConverter());
        defaultConverters.add(createJsonLdMessageConverter());
        defaultConverters.add(mappingJackson2HttpMessageConverter());
        adapter.setMessageConverters(defaultConverters);
        return adapter;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
}
