package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public class StringHandler extends BaseRDFNodeHandler<String> {


    public StringHandler(Resource resource, ExecutionContext executionContext, Setter<? super String> setter) {
        super(resource, executionContext, setter);
    }

    @Override
    String getRDFNodeValue(RDFNode node) {
        return node.toString();
    }

}
