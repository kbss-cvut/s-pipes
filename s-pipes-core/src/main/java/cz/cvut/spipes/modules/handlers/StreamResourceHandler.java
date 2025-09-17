package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.exception.ResourceNotFoundException;
import cz.cvut.spipes.registry.StreamResource;
import cz.cvut.spipes.registry.StreamResourceRegistry;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public class StreamResourceHandler extends BaseRDFNodeHandler<StreamResource> {

    public StreamResourceHandler(Resource resource, ExecutionContext executionContext, Setter<? super StreamResource> setter) {
        super(resource, executionContext, setter);
    }

    @Override
    StreamResource getRDFNodeValue(RDFNode node) {
        StreamResource res = StreamResourceRegistry.getInstance().getResourceByUrl(node.asLiteral().toString());

        if (res == null) {
            throw new ResourceNotFoundException("Stream resource " + node.asLiteral().toString() + " not found. ");
        }

        return res;
    }
}
