package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

/**
 * Created by Miroslav Blasko on 28.5.16.
 */
public class BindWithConstantModule extends AbstractModule  {



    @Override
    public ExecutionContext execute(ExecutionContext context) {
        return null;
    }

    @Override
    public void loadConfiguration(Resource moduleRes) {

    }

    public void setOutputVariable(String outputVariable) {
        return;
    }

    public void setValue(RDFNode value) {
        return;
    }
}
