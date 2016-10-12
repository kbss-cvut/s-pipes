package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.KBSS_MODULE;
import cz.cvut.sempipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.Resource;

/**
 * Created by Miroslav Blasko on 26.5.16.
 */
public class SemFormGeneratorModule extends AbstractModule {


    @Override
    public ExecutionContext executeSelf() {
        // attach existing question


        return null;
    }

    @Override
    public String getTypeURI() {
        return KBSS_MODULE.getURI() + "semform-generator";
    }

    @Override
    public void loadConfiguration() {

    }
}
