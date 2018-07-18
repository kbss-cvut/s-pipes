package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.Resource;

/**
 * Created by Miroslav Blasko on 29.5.16.
 */
public class ReturnRDFModule extends AbstractModule {

    private Resource serialization;
    private String baseURI;

    @Override
    public ExecutionContext executeSelf() {
        return executionContext;
    }

    @Override
    public String getTypeURI() {
        return SML.ReturnRDF.getURI();
    }

    @Override
    public void loadConfiguration() {
        serialization = getPropertyValue(SML.serialization).asResource();
        baseURI = getStringPropertyValue(SML.baseURI);
    }

    public Resource getSerialization() {
        return serialization;
    }

    public void setSerialization(Resource serialization) {
        this.serialization = serialization;
    }

    public String getBaseURI() {
        return baseURI;
    }

    public void setBaseURI(String baseURI) {
        this.baseURI = baseURI;
    }
}
