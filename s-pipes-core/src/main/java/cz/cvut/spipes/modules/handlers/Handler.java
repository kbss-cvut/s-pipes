package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

abstract public class Handler<T> {

    public final Resource resource;
    public final ExecutionContext executionContext;
    public final Setter<? super T> setter;

    public Handler(Resource resource, ExecutionContext executionContext, Setter<? super T> setter) {
        this.resource = resource;
        this.executionContext = executionContext;
        this.setter = setter;
    }


    abstract public void setValueByProperty(Property property);
}
