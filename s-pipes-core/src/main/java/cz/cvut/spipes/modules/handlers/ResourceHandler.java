package cz.cvut.spipes.modules.handlers;


import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public class ResourceHandler extends Handler<Resource> {
    public ResourceHandler(Resource resource, ExecutionContext executionContext, Setter<? super Resource> setter) {
        super(resource, executionContext, setter);
    }

    @Override
    public void setValueByProperty(Property property) {
        RDFNode node = getEffectiveValue(property);
        if(node != null && node.isResource()) {
            setter.addValue(node.asResource());
        }
    }
}

