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

import java.io.IOException;
import java.util.Optional;


/**
 * An abstract base class for handling RDF nodes and converting them to a specific type.
 * This class provides a common implementation for handling RDF nodes and setting their values
 * to fields of a specified type.
 *
 * @param <T> The type of the value that this handler converts RDF nodes to.
 */
abstract public class BaseRDFNodeHandler<T> extends Handler<T> {

    public BaseRDFNodeHandler(Resource resource, ExecutionContext executionContext, Setter<? super T> setter) {
        super(resource, executionContext, setter);
    }

    /**
     * Retrieves the effective RDF node value for a given property from the current resource.
     *
     * This method first attempts to retrieve the RDF node associated with the specified property from
     * the current resource. If the retrieved node is an RDF expression, it evaluates the expression
     * using the current execution context and variable bindings. If the node is not an expression, it
     * returns the node directly.
     *
     * @param property The RDF property whose value is to be retrieved. Must not be {@code null}.
     * @return The RDF node representing the effective value of the specified property. This could
     *         be the direct node value or the result of evaluating an RDF expression.
     */
    public RDFNode getEffectiveValue(Property property) {
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
     * Checks if the given property is assigned a value in the current resource.
     *
     * This method verifies whether the current resource has an RDF property assignment for the specified
     * property. It returns {@code true} if the resource has a value for the property, and {@code false}
     * otherwise. This is useful for determining if there is an existing value before attempting to
     * perform operations that depend on the presence of this property.
     *
     * @param property The RDF property to check for an assignment. Must not be {@code null}.
     * @return {@code true} if the resource has an assignment for the given property, {@code false} otherwise
     */
    boolean hasParameterValueAssignment(Property property) {
        return Optional.of(resource)
                .map(r -> r.getProperty(property))
                .isPresent();
    }

    @Override
    public void setValueByProperty(Property property) {
        RDFNode node = getEffectiveValue(property);
        if (hasParameterValueAssignment(property)) {
            try {
                if(node != null){
                    setter.addValue(getRDFNodeValue(node));
                }else{
                    setter.addValue(null);
                }

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
