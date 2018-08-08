package cz.cvut.spipes.function;

import org.apache.jena.rdf.model.Resource;


public class AbstractFunction implements Function {

    Resource resource;

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }


}
