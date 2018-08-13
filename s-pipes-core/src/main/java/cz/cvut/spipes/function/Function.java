package cz.cvut.spipes.function;

import org.apache.jena.rdf.model.Resource;

public interface Function {

    Resource getResource();
    void setResource(Resource resource);

}
