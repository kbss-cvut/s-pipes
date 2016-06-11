package cz.cvut.sempipes.function;

import org.apache.jena.rdf.model.Resource;

/**
 * Created by Miroslav Blasko on 9.6.16.
 */
public interface Function {

    public Resource getResource();
    public void setResource(Resource resource);

}
