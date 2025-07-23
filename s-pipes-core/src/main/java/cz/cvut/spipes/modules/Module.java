package cz.cvut.spipes.modules;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.VariablesBinding;
import org.apache.jena.rdf.model.Resource;

import java.io.File;
import java.util.List;

/**
 * Module should be initialized with setConfigurationResource and setInputContext.
 * Then loadConfiguration() and execute() method can be called.
 * TODO
 */
public interface Module {

    // TODO support for sparql expression
    // TODO sm:body ?

    String getTypeURI();

    String getLabel();

    void setInputContext(ExecutionContext context);

    ExecutionContext getExecutionContext();

    /**
     * TODO move to execution context
     * @param moduleResource
     */
    void setConfigurationResource(Resource moduleResource);

    /**
     * Before calling this method, the configuration resource and execution context must be already set.
     * @return
     */
    ExecutionContext execute();

    /**
     * Before calling this method, the configuration resource and execution context must be already set.
     */
    void loadConfiguration();

    Resource getResource();

    void setInputModules(List<Module> inputModules);

    List<Module> getInputModules();


    //void setOutputContext(ExecutionContext);
    // TODO execute should not return Execution context
    ExecutionContext getOutputContext();

    // TODO should not be here !!!! but rather generalized
    void addOutputBindings(VariablesBinding variablesBinding);

    /**
     *
     * @return "module" or "function" based on weather the current module is executed as a function or as a module
     */
    String getIsExecutionOf();

    /**
     *
     * @return "module" or "function" based on weather the current module is executed as a function or as a module
     */
    String getId();

    /**
     * This method returns the script file derived from the current <code>AbstractModule.executionContext</code>.
     * If <code>AbstractModule.executionContext</code>  is a function execution (i.e., execution of a pipeline pipeline),
     * the file containing the function declaration is returned. If <code>AbstractModule.executionContext</code> is a
     * module execution, the file, containing the module declaration is retuned.
     *
     * A <code>AbstractModule.executionContext</code> is function or module execution if it contains a binding of
     * variable <code>IS_EXECUTION_OF</code> with value IS_EXECUTION_OF_FUNCTION and IS_EXECUTION_OF_MODULE respectively.
     *
     * @apiNote the caller must ensure that the binding is added before executing the pipeline or the model.
     * @apiNote Downstream modules (modules executed in the pipeline before this module) can alter the bindings IS_EXECUTION_OF
     * which will may change the output AbstractModule.getScriptFile for thies execution.
     *
     * @return the script file or null if IS_EXECUTION_OF binding is missing or incorrect
     */
    File getScriptFile();
}
