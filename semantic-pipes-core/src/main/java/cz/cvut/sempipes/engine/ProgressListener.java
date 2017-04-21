package cz.cvut.sempipes.engine;

import cz.cvut.sempipes.modules.Module;

public interface ProgressListener {

    void pipelineExecutionStarted(long pipelineId);

    void pipelineExecutionFinished(long pipelineId);

    void moduleExecutionStarted(long pipelineId, String moduleExecutionId, Module outputModule, ExecutionContext inputContext, String predecessorId);

    void moduleExecutionFinished(long pipelineId, String moduleExecutionId, Module outputModule);
}
