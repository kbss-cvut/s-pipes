package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import java.nio.file.Path;
import java.nio.file.Paths;


public class PathHandler extends BaseRDFNodeHandler<Path> {
    public PathHandler(Resource resource, ExecutionContext executionContext, Setter<? super Path> setter) {
        super(resource, executionContext, setter);
    }

    @Override
    Path getRDFNodeValue(RDFNode node) {
        return Paths.get(node.toString());
    }

}
