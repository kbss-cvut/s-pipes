package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.jetbrains.annotations.NotNull;

public class RepositoryManagerHandler extends BaseRDFNodeHandler<RemoteRepositoryManager> {
    public RepositoryManagerHandler(Resource resource, ExecutionContext executionContext, Setter<? super RemoteRepositoryManager> setter) {
        super(resource, executionContext, setter);
    }

    @Override
    RemoteRepositoryManager getRDFNodeValue(@NotNull RDFNode node) {
        String rdf4jServerURL = node.asLiteral().getString();
        RemoteRepositoryManager repositoryManager = new RemoteRepositoryManager(rdf4jServerURL);
        return repositoryManager;
    }
}
