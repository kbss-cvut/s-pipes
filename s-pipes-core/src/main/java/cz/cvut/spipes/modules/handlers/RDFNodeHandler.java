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

public class RDFNodeHandler extends Handler<RDFNode> {
    public RDFNodeHandler(Resource resource, ExecutionContext executionContext, Setter<? super RDFNode> setter) {
        super(resource, executionContext, setter);
    }

    @Override
    public void setValueByProperty(Property property) {
        RDFNode valueNode = Optional.of(resource)
                .map(r -> r.getProperty(property))
                .map(Statement::getObject)
                .orElse(null);
        if (SPINExpressions.isExpression(valueNode)) {
            Resource expr = (Resource) SPINFactory.asExpression(valueNode);
            QuerySolution bindings = executionContext.getVariablesBinding().asQuerySolution();
            RDFNode node = SPINExpressions.evaluate(expr, resource.getModel(), bindings);

            setter.addValue(node);
        } else {
            setter.addValue(valueNode);
        }
    }
}
