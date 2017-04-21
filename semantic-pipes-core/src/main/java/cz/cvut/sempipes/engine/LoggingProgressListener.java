package cz.cvut.sempipes.engine;

import cz.cvut.sempipes.logging.LoggingAspect;
import cz.cvut.sempipes.modules.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingProgressListener implements ProgressListener {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingAspect.class);

    @Override
    public void pipelineExecutionStarted(long pipelineId) {
        LOG.info("pipelineExecutionStarted - pipelineId: {}", pipelineId);
    }

    @Override
    public void pipelineExecutionFinished(long pipelineId) {
        LOG.info("pipelineExecutionFinished - pipelineId: {}", pipelineId);
    }

    @Override
    public void moduleExecutionStarted(long pipelineId, String moduleExecutionId, Module outputModule, ExecutionContext inputContext, String predecessorId) {
        LOG.info("moduleExecutionStarted - pipelineId: {}, moduleExecutionId {}, inputContext: {}, predecessorId: {}", pipelineId,
            moduleExecutionId, inputContext, predecessorId);
    }

    @Override
    public void moduleExecutionFinished(long pipelineId, String moduleExecutionId, Module outputModule) {
        LOG.info("moduleExecutionFinished - pipelineId: {}, moduleExecutionId: {}", pipelineId, moduleExecutionId);
    }
}
