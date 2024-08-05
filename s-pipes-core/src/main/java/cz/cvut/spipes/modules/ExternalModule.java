package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContext;

public class ExternalModule extends AnnotatedAbstractModule {

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


//    public OutputStream executeExternalProgram(InputStream inputStream ) {
//
//    }


}
