package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.KBSS_MODULE;
import cz.cvut.sempipes.engine.ExecutionContext;

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
        return KBSS_MODULE.semform_generator.getURI();
    }

    @Override
    public void loadConfiguration() {

    }
}
