package cz.cvut.sempipes.engine;

import cz.cvut.sempipes.modules.Module;
import org.apache.jena.rdf.model.Model;

/**
 * Created by blcha on 6.5.16.
 */
public interface ExecutionEngine {

    void loadConfiguration(Model config);

    // TODO web service have injected execution context
    ExecutionContext executePipeline(Module m, ExecutionContext context);
}
