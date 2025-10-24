package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public class RDFNodeHandler extends BaseRDFNodeHandler<RDFNode> {
    public RDFNodeHandler(Resource resource, ExecutionContext executionContext, Setter<? super RDFNode> setter) {
        super(resource, executionContext, setter);
    }

    @Override
    RDFNode getRDFNodeValue(RDFNode node) {
        return node;
    }


}
