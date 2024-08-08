package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.spin.model.Select;

public class SelectHandler extends Handler<Select> {

    public SelectHandler(Resource resource, ExecutionContext executionContext, Setter<? super Select> setter) {
        super(resource, executionContext, setter);
    }

    @Override
    public void setValueByProperty(Property property) {
        RDFNode node = getEffectiveValue(property);
        if (node != null) {
            setter.addValue(node.asResource().as(Select.class));
        }
    }
}
