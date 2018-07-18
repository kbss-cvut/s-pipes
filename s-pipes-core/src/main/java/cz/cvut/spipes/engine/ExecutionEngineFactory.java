package cz.cvut.spipes.engine;

import cz.cvut.spipes.logging.SemanticLoggingProgressListener;

/**
 * Created by Miroslav Blasko on 31.5.16.
 */
public class ExecutionEngineFactory {
    public static ExecutionEngine createEngine() {
        final ExecutionEngine e = new ExecutionEngineImpl();
        e.addProgressListener(new LoggingProgressListener());
        e.addProgressListener(new SemanticLoggingProgressListener());
        return e;
    }
}
