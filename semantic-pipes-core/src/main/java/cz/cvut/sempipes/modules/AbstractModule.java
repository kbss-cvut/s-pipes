package cz.cvut.sempipes.modules;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.reasoner.ReasonerRegistry;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Miroslav Blasko on 10.5.16.
 */
public abstract class AbstractModule implements Module {

    Resource resource;
    List<Module> inputModules = new LinkedList<>();

    // load each properties
    // valiadation of required parameter
    // ?? validation of shape of input graph


    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public List<Module> getInputModules() {
        return inputModules;
    }

    @Override
    public void setInputModules(List<Module> inputModules) {
        this.inputModules = inputModules;
    }

    boolean getPropertyValue(String propertyStr, boolean defaultValue) {

        Statement s = resource.getProperty(getProperty(propertyStr));

        if (s != null && s.getObject().isLiteral()) {
            //TODO check if it is boolean first
            return s.getBoolean();
        }
        return defaultValue;
    }

    // TODO move to utils
    Property getProperty(String propertyStr) {
        return resource.getModel().createProperty(propertyStr);
    }

    public String getStringFromProperty(String propertyResourceUri) {
        return resource.getProperty(resource.getModel().createProperty(propertyResourceUri)).getObject().toString();
    }
}
