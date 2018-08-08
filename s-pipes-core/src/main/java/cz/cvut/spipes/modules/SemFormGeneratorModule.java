package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.engine.ExecutionContext;

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
