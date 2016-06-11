package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.Resource;

import java.util.List;

/**
 * Module should be initialized with setConfigurationResource and setExecutionContext.
 * Then loadConfiguration() and execute() method can be called.
 * TODO
 * Created by blcha on 6.5.16.
 */
public interface Module {

    // TODO support for sparql expression
    // TODO sm:body ?

    String getLabel();

    void setExecutionContext(ExecutionContext context);

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

    //setInputContext()
    //getOutputContext()
}
