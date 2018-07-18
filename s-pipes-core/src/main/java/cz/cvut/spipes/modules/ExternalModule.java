package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContext;

/**
 * Created by Miroslav Blasko on 18.5.16.
 */
public class ExternalModule extends AbstractModule {

    // path
    String externalModulePath;

    String programCall;

    @Override
    public ExecutionContext executeSelf() {
        return null;
    }

    @Override
    public String getTypeURI() {
        return "http://external-module.com";
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
