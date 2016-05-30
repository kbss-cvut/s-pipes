package cz.cvut.sempipes.engine;

import cz.cvut.sempipes.modules.Module;
import org.apache.jena.rdf.model.Model;

/**
 * Created by Miroslav Blasko on 30.5.16.
 */
public class ExecutionEngineImpl implements ExecutionEngine {

    // TODO progress monitor

    public void loadConfiguration(Model config) {
        return;
    }


    public ExecutionContext executePipeline(Module m, ExecutionContext context) {
        // load
        // build plan
        // execute each module
        return null;
    }

    // TODO differentiate output, input modules ?
    public ExecutionContext executeModule(Module m, ExecutionContext context) {
        // load
        // execute module
        return m.execute(context);
    }
}

