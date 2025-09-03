package cz.cvut.spipes.modules.handlers;


import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public class ResourceHandler extends BaseRDFNodeHandler<Resource> {
    public ResourceHandler(Resource resource, ExecutionContext executionContext, Setter<? super Resource> setter) {
        super(resource, executionContext, setter);
    }

    @Override
    Resource getRDFNodeValue(RDFNode node) {
        return node.asResource();
    }

}

