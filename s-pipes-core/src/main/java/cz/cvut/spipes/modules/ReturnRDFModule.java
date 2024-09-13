package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.Resource;

public class ReturnRDFModule extends AnnotatedAbstractModule {
    @Parameter(iri = SML.serialization)
    private Resource serialization;
    @Parameter(iri = SML.baseURI)
    private String baseURI;

    @Override
    public ExecutionContext executeSelf() {
        return executionContext;
    }

    @Override
    public String getTypeURI() {
        return SML.ReturnRDF;
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
