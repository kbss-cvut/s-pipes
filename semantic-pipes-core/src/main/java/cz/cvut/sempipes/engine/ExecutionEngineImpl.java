package cz.cvut.sempipes.engine;

import cz.cvut.sempipes.modules.Module;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Miroslav Blasko on 30.5.16.
 */
class ExecutionEngineImpl implements ExecutionEngine {


    private static Logger LOG = LoggerFactory.getLogger(ExecutionEngineImpl.class);

    // TODO progress monitor

    public void loadConfiguration(Model config) {
        return;
    }


    public ExecutionContext executePipeline(Module m, ExecutionContext context) {
        LOG.info("Executing script " + m.getResource());
        return _executePipeline(m, context);
    }

    // TODO differentiate output, input modules ?
    public ExecutionContext executeModule(Module m, ExecutionContext context) {
        LOG.info("Executing module " + m.getResource());
        return m.execute(context);
    }

    private ExecutionContext _executePipeline(Module module, ExecutionContext context) {
        // module has no predeccesor
        if (module.getInputModules().isEmpty()) {
            return module.execute(context);
        }

        Map<Resource, ExecutionContext> resource2ContextMap = module.getInputModules().stream()
                .collect(Collectors.toMap(Module::getResource, mod -> this._executePipeline(mod, context)));

        LOG.info("\t - " + module.getLabel());

        return mergeContexts(resource2ContextMap);
    }


    // TODO optimize :
    //      1) dynamic union -- ModelFactory.createUnion()
    //      2) modules should not modify Model but create new ones + immutable Model
    private ExecutionContext mergeContexts(Map<Resource, ExecutionContext> resource2ContextMap) {

        Model newModel = ModelFactory.createDefaultModel();
        VariablesBinding variablesBinding = new VariablesBinding();


        resource2ContextMap.entrySet().stream().forEach(e -> {

            Resource modRes = e.getKey();
            ExecutionContext context = e.getValue();

            // merge models
            newModel.add(context.getDefaultModel());

            // merge variable bindings
            VariablesBinding b = e.getValue().getVariablesBinding();

            VariablesBinding conflictingBinding = variablesBinding.extendConsistently(b);

            if (conflictingBinding.isEmpty()) {
                LOG.warn("Module {} has conflicting variables binding {} with sibling modules ocurring in pipeline. ", modRes, context);
            }
        });


        return ExecutionContextFactory.createContext(newModel, variablesBinding);
    }

}

