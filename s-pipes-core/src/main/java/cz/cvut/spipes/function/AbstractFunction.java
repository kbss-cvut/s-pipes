package cz.cvut.spipes.function;

import org.apache.jena.rdf.model.Resource;

/**
 * Created by Miroslav Blasko on 9.6.16.
 */
public class AbstractFunction implements Function {

    Resource resource;

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }


}
