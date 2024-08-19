package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.util.SPINExpressions;

import java.util.Optional;

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
     * Sets the value of the native Java object based on the provided RDF property.
     * If the value is an RDF expression which evaluates to undefined/null,
     * the setter should not be triggered or set null as well.
     *
     * @param property the RDF property to extract the value from
     */
    abstract public void setValueByProperty(Property property);

}
