package cz.cvut.sempipes.engine;

import cz.cvut.sempipes.logging.SemanticLoggingProgressListener;

/**
 * Created by Miroslav Blasko on 31.5.16.
 */
public class ExecutionEngineFactory {
    public static ExecutionEngine createEngine() {
        final ExecutionEngine e = new ExecutionEngineImpl();
        e.addProgressListener(new LoggingProgressListener());
//        e.addProgressListener(new SemanticLoggingProgressListener()); //TODO disabled due to a bug #589
        return e;
    }
}
