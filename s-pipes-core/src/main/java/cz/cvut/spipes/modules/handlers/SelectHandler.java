package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import cz.cvut.spipes.spin.model.Select;

public class SelectHandler extends BaseRDFNodeHandler<Select> {

    public SelectHandler(Resource resource, ExecutionContext executionContext, Setter<? super Select> setter) {
        super(resource, executionContext, setter);
    }

    @Override
    Select getRDFNodeValue(RDFNode node) {
        return node.asResource().as(Select.class);
    }

}
