package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.exception.ScriptRuntimeErrorException;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.util.SPINExpressions;

import java.util.Optional;


/**
 * An abstract base class for handling RDF nodes and converting them to a specific type.
 * This class provides a common implementation for handling RDF nodes and setting their values
 * to fields of a specified type.
 *
 * @param <T> The type of the value that this handler converts RDF nodes to.
 */
abstract public class BaseRDFNodeHandler<T>  extends Handler<T> {

    public BaseRDFNodeHandler(Resource resource, ExecutionContext executionContext, Setter<? super T> setter) {
        super(resource, executionContext, setter);
    }

    protected RDFNode getEffectiveValue(Property property){
        RDFNode valueNode = Optional.of(resource)
                .map(r -> r.getProperty(property))
                .map(Statement::getObject)
                .orElse(null);
        if (SPINExpressions.isExpression(valueNode)) {
            Resource expr = (Resource) SPINFactory.asExpression(valueNode);
            QuerySolution bindings = executionContext.getVariablesBinding().asQuerySolution();
            return SPINExpressions.evaluate(expr, resource.getModel(), bindings);
        } else {
            return valueNode;
        }
    }

    /**
     * Converts an RDF node to a value of type {@code T}.
     * This method must be implemented by subclasses to provide the specific conversion logic.
     *
     * @param node The RDF node to convert.
     * @return The converted value of type {@code T}.
     */
    abstract T getRDFNodeValue(RDFNode node) throws Exception;

    /**
     * Sets a value to the field associated with this handler by converting an RDF node
     * retrieved from the specified RDF property.
     *
     * @param property The RDF property whose value is used to retrieve the RDF node.
     * @throws ScriptRuntimeErrorException If an error occurs during the conversion or setting process.
     */
    @Override
    public void setValueByProperty(Property property) {
        RDFNode node = getEffectiveValue(property);

        if (node != null) {
            try {
                setter.addValue(getRDFNodeValue(node));
            } catch (Exception ex) {
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
