package cz.cvut.spipes.modules.handlers;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.modules.Mode;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

public class ModeHandler extends BaseRDFNodeHandler<Mode>  {

    public ModeHandler(Resource resource, ExecutionContext executionContext, Setter<? super Mode> setter) {
        super(resource, executionContext, setter);

    }

    @Override
    Mode getRDFNodeValue(RDFNode node) throws Exception {
        return Mode.fromResource(node.asResource());
    }
}
