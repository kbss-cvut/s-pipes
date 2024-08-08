package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

public class IntegerHandler extends Handler<Integer>{

    public IntegerHandler(Resource resource, ExecutionContext executionContext, Setter<? super Integer> setter) {
        super(resource, executionContext, setter);
    }

    @Override
    public void setValueByProperty(Property property) {
        RDFNode node = getEffectiveValue(property);
        if (node != null && node.isLiteral()) {
            setter.addValue(node.asLiteral().getInt());
        }
    }
}
