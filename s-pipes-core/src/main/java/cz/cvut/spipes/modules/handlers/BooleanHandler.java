package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;


public class BooleanHandler extends BaseRDFNodeHandler<Boolean> {
    public BooleanHandler(Resource resource, ExecutionContext executionContext, Setter<? super Boolean> setter) {
        super(resource, executionContext, setter);
    }

    @Override
    Boolean getRDFNodeValue(@NotNull RDFNode node) {
        return node.asLiteral().getBoolean();
    }
}
