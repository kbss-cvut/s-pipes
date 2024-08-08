package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public class RDFNodeHandler extends Handler<RDFNode> {
    public RDFNodeHandler(Resource resource, ExecutionContext executionContext, Setter<? super RDFNode> setter) {
        super(resource, executionContext, setter);
    }

    @Override
    public void setValueByProperty(Property property) {
        RDFNode node = getEffectiveValue(property);
        if(node != null) {
            setter.addValue(node);
        }
    }
}
