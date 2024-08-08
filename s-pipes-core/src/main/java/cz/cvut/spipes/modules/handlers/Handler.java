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
}
