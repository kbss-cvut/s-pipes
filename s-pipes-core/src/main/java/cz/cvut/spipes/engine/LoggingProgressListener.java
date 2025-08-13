package cz.cvut.spipes.engine;

import cz.cvut.spipes.modules.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingProgressListener implements ProgressListener {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingProgressListener.class);

    @Override
    public void pipelineExecutionStarted(long pipelineExecutionId, final String functionName, final String scriptPath, final String script) {
        LOG.debug("pipelineExecutionStarted - pipelineExecutionId: {}", pipelineExecutionId);
    }

    @Override
    public void pipelineExecutionFinished(long pipelineExecutionId) {
        LOG.debug("pipelineExecutionFinished - pipelineExecutionId: {}", pipelineExecutionId);
    }

    @Override
    public void pipelineExecutionFailed(long pipelineExecutionId) {
        LOG.debug("pipelineExecutionFailed - pipelineExecutionId: {}", pipelineExecutionId);
    }

    @Override
    public void moduleExecutionStarted(long pipelineExecutionId, String moduleExecutionId, Module outputModule, ExecutionContext inputContext, String predecessorModuleExecutionId) {
        LOG.debug("moduleExecutionStarted - pipelineExecutionId: {}, moduleExecutionId: {}, inputContext: {}, predecessorModuleExecutionId: {}", pipelineExecutionId,
            moduleExecutionId, inputContext, predecessorModuleExecutionId);
    }

    @Override
    public void moduleExecutionFinished(long pipelineExecutionId, String moduleExecutionId, Module outputModule) {
        LOG.debug("moduleExecutionFinished - pipelineExecutionId: {}, moduleExecutionId: {}", pipelineExecutionId, moduleExecutionId);
    }
}
