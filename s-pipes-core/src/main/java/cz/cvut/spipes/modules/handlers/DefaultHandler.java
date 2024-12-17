package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

/**
 * The default implementation of the {@link Handler} class
 * @see Handler
 */
public class DefaultHandler extends Handler<Object> {

    public DefaultHandler(Resource resource, ExecutionContext executionContext, Setter<Object> setter) {
        super(resource, executionContext, setter);
    }

    @Override
    public void setValueByProperty(Property property) {

    }
}
