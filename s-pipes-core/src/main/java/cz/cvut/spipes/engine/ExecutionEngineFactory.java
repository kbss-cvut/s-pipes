package cz.cvut.spipes.engine;

public class ExecutionEngineFactory {
    public static ExecutionEngine createEngine() {
        final ExecutionEngine e = new ExecutionEngineImpl();
        e.addProgressListener(new LoggingProgressListener());
        return e;
    }
}
