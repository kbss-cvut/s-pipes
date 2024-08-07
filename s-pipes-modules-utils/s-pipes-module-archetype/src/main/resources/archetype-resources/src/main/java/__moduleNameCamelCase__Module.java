## Convenience variables for Maven Archetype
#set($className = "${moduleNameCamelCase}Module")
##
package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SPipesModule(label = "$artifactId", comment = "Automatically generated s-pipes module. For more information, see $className.java.")
public class $className extends AnnotatedAbstractModule {
    private static final Logger LOG = LoggerFactory.getLogger(${className}.class);

    private static final String TYPE_URI = KBSS_MODULE.uri + "$artifactId";
    private static final String TYPE_PREFIX = TYPE_URI + "/";

    @Override
    protected ExecutionContext executeSelf() {
        return executionContext;
    }

    @Override
    public String getTypeURI() {
        return KBSS_MODULE.uri + "$className";
    }

    @Override
    public void loadConfiguration() {
    }
}
