package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.exception.ScriptRuntimeErrorException;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;


/**
 * Abstract Handler for cases when native Java value can be extracted directly from a single RDFNode.
 */
abstract public class BaseRdfNodeHandler<T>  extends Handler<T> {

    public BaseRdfNodeHandler(Resource resource, ExecutionContext executionContext, Setter<? super T> setter) {
        super(resource, executionContext, setter);
    }

    /**
     * Constructs the native Java value from a given RDFNode.
     *
     * @param  node  the RDFNode to extract the native Java value from
     * @return      the native Java value of the RDFNode
     */
    abstract T getJavaNativeValue(RDFNode node);

    @Override
    public void setValueByProperty(Property property) {
        RDFNode node = getEffectiveValue(property);

        if (node != null) {
            try {
                setter.addValue(getJavaNativeValue(node));
            } catch (RuntimeException ex) {
                throw new ScriptRuntimeErrorException(
                    String.format("""
                            Failed to set value of the field `%s` of type `%s` within class `%s`.
                            The value was suppose to be converted from RDF node `%s` which was computed
                            from RDF statement <?s, ?p, ?o> where:
                            - s = `%s`,
                            - p = `%s`,
                            """,
                        setter.getField().getName(),
                        setter.getField().getType(),
                        setter.getField().getDeclaringClass().getName(),
                        node,
                        resource,
                        property
                    ),
                    ex
                );
            }
        }
    }
}
