package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.engine.ExecutionContext;

/**
 * Created by Miroslav Blasko on 18.5.16.
 */
public class ExternalModule extends AbstractModule {

    // path
    String externalModulePath;

    String programCall;

    @Override
    public ExecutionContext execute() {
        return null;
    }

    @Override
    public void loadConfiguration() {
        // load external module path
        // load config
    }

//    public OutputStream executeExternalProgram(InputStream inputStream ) {
//
//    }


}
