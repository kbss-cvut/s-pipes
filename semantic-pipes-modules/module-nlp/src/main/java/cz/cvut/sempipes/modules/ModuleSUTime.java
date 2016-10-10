package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.KBSS_MODULE;
import cz.cvut.sempipes.engine.ExecutionContext;

/**
 * Created by Miroslav Blasko on 10.10.16.
 */
public class ModuleSUTime extends AbstractModule {

    @Override
    public String getTypeURI() {
        return KBSS_MODULE.su_time.getURI();
    }


    @Override
    public void loadConfiguration() {
    }

    @Override
    ExecutionContext executeSelf() {
        return null;
    }
}
