package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO Order of queries is not enforced.
 */
@SPipesModule(label = "apply construct v2", comment = "Generates triples from input model using specified constructQueries.")
public class ApplyConstructV2Module extends ApplyConstructAbstractModule {

    private static final String TYPE_URI = KBSS_MODULE.uri + "apply-construct-v2";

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }


    @Override
    public void loadManualConfiguration() {
        super.loadConfiguration();
        iterationCount = this.getPropertyValue(KBSS_MODULE.JENA.has_max_iteration_count, 1);
    }
}
