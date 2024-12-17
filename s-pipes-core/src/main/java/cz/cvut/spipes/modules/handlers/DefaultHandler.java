package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

/**
 * The {@link DefaultHandler} class is a default implementation of the {@link Handler} interface.
 * It uses the {@link HandlerRegistry} to retrieve the appropriate handler for the given field type.
 **/
public class DefaultHandler extends Handler {

    private Handler<?> typeHandler;

    public DefaultHandler(Resource resource, ExecutionContext executionContext, Setter setter) {
        super(resource, executionContext, setter);

        HandlerRegistry handlerRegistry = HandlerRegistry.getInstance();
        typeHandler = handlerRegistry.getHandler(setter.getField().getType(), resource, executionContext, setter);
    }

    @Override
    public void setValueByProperty(Property property) {
        typeHandler.setValueByProperty(property);
    }
}
