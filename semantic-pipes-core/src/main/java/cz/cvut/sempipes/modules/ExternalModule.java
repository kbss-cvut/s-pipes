package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;


import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Miroslav Blasko on 18.5.16.
 */
public class ExternalModule extends AbstractModule {

    // path
    String externalModulePath;

    String programCall;

    @Override
    public ExecutionContext execute(ExecutionContext context) {
        return null;
    }

    @Override
    public void loadConfiguration(Resource module) {
        // load external module path
        // load config
    }

//    public OutputStream executeExternalProgram(InputStream inputStream ) {
//
//    }


}
