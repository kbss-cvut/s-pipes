package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;

public class ReturnParameterModule extends AbstractModule{
    public static final String TYPE_URI = KBSS_MODULE.uri + "Return-parameter-module";
    public static final String TYPE_PREFIX = TYPE_URI + "/";
    @Override
    ExecutionContext executeSelf() {
        return this.executionContext;
    }

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

    @Override
    public void loadConfiguration() {

    }
}
