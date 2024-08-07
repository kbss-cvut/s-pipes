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
        RDFNode node = getRDFNodeByProperty(property);
        if(node != null) {
            setter.addValue(node);
        }
    }
}
