package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.exception.ScriptRuntimeErrorException;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

/**
 * Abstract class for handler that is responsible to load native Java value
 * from its RDF representation and set this value using provided {@link Setter}.
 *
 * The class is initialized with:
 * - {@link Resource} and {@link ExecutionContext} which provides partial context from which RDF value can be extracted
 * - {@link Setter} which is used to set native Java value into exactly one Java field
 *
 * @param <T> the type of native Java value being handled
 */
abstract public class Handler<T> {

    protected final Resource resource;
    protected final ExecutionContext executionContext;
    protected final Setter<? super T> setter;

    public Handler(Resource resource, ExecutionContext executionContext, Setter<? super T> setter) {
        this.resource = resource;
        this.executionContext = executionContext;
        this.setter = setter;
    }

    /**
     * Sets a value to the field associated with this handler by converting an RDF node
     * retrieved from the specified RDF property.
     *
     * @param property The RDF property whose value is used to retrieve the RDF node.
     * @throws ScriptRuntimeErrorException If an error occurs during the conversion or setting process.
     */
    abstract public void setValueByProperty(Property property);

}
