## Convenience variables for Maven Archetype
#set($className = "${moduleNameCamelCase}Module")
##
package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.engine.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class $className extends AnnotatedAbstractModule {
    private static final Logger LOG = LoggerFactory.getLogger(${className}.class);

    private static final String TYPE_URI = KBSS_MODULE.uri + "$artifactId";
    private static final String TYPE_PREFIX = TYPE_URI + "/";

// todo remove
//    @Parameter(urlPrefix = TYPE_PREFIX, name = "endpoint-url")
//    private String endpointUrl;
//
//    @Parameter(urlPrefix = TYPE_PREFIX, name = "output-resource-variable")
//    private String outputResourceVariable;
//
//    public String getEndpointUrl() {
//        return endpointUrl;
//    }
//
//    public void setEndpointUrl(String endpointUrl) {
//        this.endpointUrl = endpointUrl;
//    }
//
//    public String getOutputResourceVariable() {
//        return outputResourceVariable;
//    }
//
//    public void setOutputResourceVariable(String outputResourceVariable) {
//        this.outputResourceVariable = outputResourceVariable;
//    }


    @Override
    protected ExecutionContext executeSelf() {
        return executionContext;
    }

    @Override
    public String getTypeURI() {
        return KBSS_MODULE.getURI() + "$className";
    }

    @Override
    public void loadConfiguration() {
        LOG.info("Configuration loaded successfully.");
    }
}
