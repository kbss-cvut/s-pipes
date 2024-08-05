package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.Resource;

public class ReturnRDFModule extends AnnotatedAbstractModule {
    @Parameter(urlPrefix = SML.uri, name = "serialization", comment = "")
    private Resource serialization;
    @Parameter(urlPrefix = SML.uri, name = "baseURI", comment = "")
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
