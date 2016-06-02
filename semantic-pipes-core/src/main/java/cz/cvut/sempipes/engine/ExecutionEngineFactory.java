package cz.cvut.sempipes.engine;

/**
 * Created by Miroslav Blasko on 31.5.16.
 */
public class ExecutionEngineFactory {
    public static ExecutionEngine createEngine() {
        return new ExecutionEngineImpl();
    }
}
