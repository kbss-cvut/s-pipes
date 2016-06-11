package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.engine.ExecutionContext;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDFS;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.util.SPINExpressions;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Miroslav Blasko on 10.5.16.
 */
public abstract class AbstractModule implements Module {

    Resource resource;
    List<Module> inputModules = new LinkedList<>();
    ExecutionContext executionContext;

    // load each properties
    // valiadation of required parameter
    // ?? validation of shape of input graph

    @Override
    public void setExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    @Override
    public void setConfigurationResource(Resource resource) {
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }

    public String getLabel() {
        RDFNode labelNode =  resource.getProperty(RDFS.label).getObject();
        return ((labelNode != null) && labelNode.isLiteral()) ? labelNode.toString() : resource.asResource().getLocalName();
    }


    @Override
    public List<Module> getInputModules() {
        return inputModules;
    }

    @Override
    public void setInputModules(List<Module> inputModules) {
        this.inputModules = inputModules;
    }

    RDFNode getPropertyValue(Property property) {
        return resource.getProperty(property).getObject();
    }

    boolean getPropertyValue(Property property, boolean defaultValue) {

        Statement s = resource.getProperty(property);

        if (s != null && s.getObject().isLiteral()) {
            //TODO check if it is boolean first
            return s.getBoolean();
        }
        return defaultValue;
    }

    public String getStringPropertyValue(Property property) {
        return resource.getProperty(property).getObject().toString();
    }

    protected RDFNode getEffectiveValue(Property valueProperty) {
        RDFNode valueNode = resource.getProperty(valueProperty).getObject();
        if (SPINExpressions.isExpression(valueNode)) {
            Resource expr = (Resource) SPINFactory.asExpression(valueNode);
            QuerySolution bindings = executionContext.getVariablesBinding().asQuerySolution();
            return SPINExpressions.evaluate(expr, resource.getModel(), bindings); //TODO resource.getModel() should be part o context
        } else {
            return valueNode;
        }
    }

//    @Override
//    public String toString() {
//        String resourceId = (resource  != null) ? ( " (" + resource.getId() + ")") : "";
//        return resourceId;
//    }
}
