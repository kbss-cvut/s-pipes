package cz.cvut.spipes.engine;

import cz.cvut.spipes.modules.Module;

/**
 * Created by blcha on 6.5.16.
 */
public interface ExecutionEngine {

    // TODO web service have injected execution context
    ExecutionContext executePipeline(Module m, ExecutionContext context);

    /**
     * Adds execution progress listener.
     *
     * @param listener to add
     */
    void addProgressListener(ProgressListener listener);

    /**
     * Removes execution progress listener.
     *
     * @param listener to remove
     */
    void removeProgressListener(ProgressListener listener);
}
