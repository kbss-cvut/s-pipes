package cz.cvut.spipes.engine;

import cz.cvut.spipes.modules.Module;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

class ExecutionEngineImpl implements ExecutionEngine {

    private static Logger log = LoggerFactory.getLogger(ExecutionEngineImpl.class);

    private Set<ProgressListener> listeners = new HashSet<>();

    private static int i = 0 ;

    public ExecutionContext executePipeline(final Module module, final ExecutionContext inputContext) {
        log.info("Executing script {} with context {}.", module.getResource(), inputContext.toSimpleString());
        final long pipelineExecutionId = Instant.now().toEpochMilli()*1000+(i++);

        fire((l) -> {l.pipelineExecutionStarted(pipelineExecutionId); return null;});
        ExecutionContext outputContext = _executePipeline(pipelineExecutionId, module, inputContext, null);
        fire((l) -> {l.pipelineExecutionFinished(pipelineExecutionId); return null;});
        return outputContext;
    }

    private void fire(final Function<ProgressListener,Void> function) {
        listeners.forEach( (listener) -> {
            try {
                function.apply(listener);
            } catch(final Exception e) {
                log.warn("Listener {} failed.", listener, e);
            }
        });
    }

    private ExecutionContext _executePipeline(long pipelineExecutionId, Module module, ExecutionContext context, String predecessorId) {
        final String moduleExecutionId = pipelineExecutionId + "-"+module.hashCode() + "-"+context.hashCode();


        // module has run already
        if (module.getOutputContext() != null) {
            module.addOutputBindings(context.getVariablesBinding());
            fire((l) -> {l.moduleExecutionFinished(pipelineExecutionId, moduleExecutionId, module); return null;});
            return module.getOutputContext();
        }

        // module has no predeccesor
        if (module.getInputModules().isEmpty()) {
            fire((l) -> {l.moduleExecutionStarted(pipelineExecutionId, moduleExecutionId, module, context, predecessorId); return null;});

            if (module.getExecutionContext() != null) {
                log.debug("Execution context for module {} already set.", module);
            } else {
                module.setInputContext(context);

                log.info(" ##### " + module.getLabel());
                if (log.isTraceEnabled()) {
                    log.trace("Using input context {}", context.toTruncatedSimpleString()); //TODO redundant code -> merge
                }
                ExecutionContext outputContext = module.execute();
                if (log.isTraceEnabled()) {
                    log.trace("Returning output context {}", outputContext.toSimpleString());
                }
                module.addOutputBindings(context.getVariablesBinding());
            }
            fire((l) -> {l.moduleExecutionFinished(pipelineExecutionId, moduleExecutionId, module); return null;});
            return module.getOutputContext();
        }

        Map<Resource, ExecutionContext> resource2ContextMap = module.getInputModules().stream()
                .collect(Collectors.toMap(Module::getResource, mod -> this._executePipeline(pipelineExecutionId, mod, context, moduleExecutionId)));


        log.info(" ##### " + module.getLabel());
        ExecutionContext mergedContext = mergeContexts(resource2ContextMap);
        if (log.isTraceEnabled()) {
            log.trace("Using input merged context {}", mergedContext.toTruncatedSimpleString());
        }
        fire((l) -> {l.moduleExecutionStarted(pipelineExecutionId, moduleExecutionId, module, mergedContext, predecessorId); return null;});

        module.setInputContext(mergedContext);

        ExecutionContext outputContext = module.execute();
        if (log.isTraceEnabled()) {
            log.trace("Returning output context {}", outputContext.toSimpleString());
        }
        module.addOutputBindings(mergedContext.getVariablesBinding());
        fire((l) -> {l.moduleExecutionFinished(pipelineExecutionId, moduleExecutionId, module); return null;});
        return module.getOutputContext();
    }

    private ExecutionContext createMergedExecutionContext(ExecutionContext executionContext, VariablesBinding additionalVariablesBinding) {
        VariablesBinding mergedVarsBinding = new VariablesBinding(executionContext.getVariablesBinding().asQuerySolution());
        mergedVarsBinding.extendConsistently(additionalVariablesBinding);
        return ExecutionContextFactory.createContext(executionContext.getDefaultModel(), mergedVarsBinding);
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

            if (! conflictingBinding.isEmpty()) {
                log.warn("Module {} has conflicting variables binding {} with sibling modules ocurring in pipeline. ", modRes, context);
            }
        });


        return ExecutionContextFactory.createContext(newModel, variablesBinding);
    }

    @Override
    public void addProgressListener(final ProgressListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeProgressListener(final ProgressListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }
}