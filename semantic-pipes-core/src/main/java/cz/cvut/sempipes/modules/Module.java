package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.Resource;

import java.util.List;

/**
 * Created by blcha on 6.5.16.
 */
public interface Module {

    // TODO support for sparql expression
    // TODO sm:body ?

    ExecutionContext execute(ExecutionContext context);

    void loadConfiguration(Resource moduleRes);

    void setInputModules(List<Module> inputModules);
    List<Module> getInputModules();
}
