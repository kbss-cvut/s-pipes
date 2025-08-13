package cz.cvut.spipes.engine;

import cz.cvut.spipes.modules.Module;

/**
 * Progress listener applicable to pipeline executions.
 * <p>
 * Following variables are defined within interface:
 * pipelineExecutionId -- is unique id whose identity is defined by event of execution of a pipeline.
 * moduleExecutionId -- is unique id whose identity is defined by pipeline execution, module's instance and input context of the module.
 */
public interface ProgressListener {

    /**
     * Triggers when execution of a pipeline starts.
     *
     * @param pipelineExecutionId execution id of the pipeline
     */
    void pipelineExecutionStarted(long pipelineExecutionId, final String functionName, final String scriptPath, final String script);

    /**
     * Triggers when execution of a pipeline finishes.
     *
     * @param pipelineExecutionId execution id of the pipeline
     */
    void pipelineExecutionFinished(long pipelineExecutionId);

    void pipelineExecutionFailed(long pipelineExecutionId);

    /**
     * Triggers when execution of a module within a pipeline starts.
     *
     * @param pipelineExecutionId execution id of the pipeline
     * @param moduleExecutionId execution id of the module
     * @param outputModule the module whose execution starts
     * @param inputContext input context provided to the module
     * @param predecessorModuleExecutionId execution id of a module that triggered execution
     *                                    of this module. This module will be executed before
     *                                     its predecessor module as the predecessor module might
     *                                      use the output of this module.
     */
    void moduleExecutionStarted(long pipelineExecutionId, String moduleExecutionId, Module outputModule, ExecutionContext inputContext, String predecessorModuleExecutionId);

    /**
     * Triggers when execution of a module within the pipeline finishes.
     *
     * @param pipelineExecutionId execution id of the pipeline
     * @param moduleExecutionId the output module that will be executed
     * @param outputModule the module whose execution finished
     */
    void moduleExecutionFinished(long pipelineExecutionId, String moduleExecutionId, Module outputModule);
}
