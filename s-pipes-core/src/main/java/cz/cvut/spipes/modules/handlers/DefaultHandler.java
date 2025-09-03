package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

/**
 * The {@link DefaultHandler} class is the default implementation of the {@link Handler} interface.
 * It delegates the handling responsibility to the {@link Handler} class registered for the field type
 * within the {@link HandlerRegistry}.
 **/
public class DefaultHandler<T> extends Handler<T> {

    private final Handler<?> typeHandler;

    public DefaultHandler(Resource resource, ExecutionContext executionContext, Setter<? super T> setter) {
        super(resource, executionContext, setter);

        HandlerRegistry handlerRegistry = HandlerRegistry.getInstance();
        typeHandler = handlerRegistry.getHandler(setter.getField().getType(), resource, executionContext, setter);
    }

    @Override
    public void setValueByProperty(Property property) {
        typeHandler.setValueByProperty(property);
    }
}
