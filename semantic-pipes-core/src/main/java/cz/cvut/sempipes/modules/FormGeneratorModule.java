package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.engine.ExecutionContext;

/**
 * Created by Miroslav Blasko on 26.5.16.
 */
public class FormGeneratorModule extends AbstractModule {


    @Override
    public ExecutionContext executeSelf() {

        return null;
    }

    @Override
    public String getTypeURI() {
        return "http://form-generator-module";
    }

    @Override
    public void loadConfiguration() {

    }
}
