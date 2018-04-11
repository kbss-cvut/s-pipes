package cz.cvut.sempipes.function;

import org.apache.jena.rdf.model.Resource;

/**
 * Created by Miroslav Blasko on 9.6.16.
 */
public interface Function {

    Resource getResource();
    void setResource(Resource resource);

}
