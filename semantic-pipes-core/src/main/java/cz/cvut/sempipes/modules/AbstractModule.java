package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.engine.ExecutionContext;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDFS;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.util.SPINExpressions;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Miroslav Blasko on 10.5.16.
 */
public abstract class AbstractModule implements Module {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractModule.class);
    Resource resource;
    List<Module> inputModules = new LinkedList<>();
    ExecutionContext executionContext;
    ExecutionContext outputContext;


    // load each properties
    // valiadation of required parameter
    // ?? validation of shape of input graph


    abstract ExecutionContext executeSelf();

    @Override
    public ExecutionContext execute() {
        loadConfiguration();
        outputContext = executeSelf();
        return  outputContext;
    }

    @Override
    public ExecutionContext getOutputContext() {
        return outputContext;
    }

    @Override
    public void setExecutionContext(ExecutionContext executionContext) {
        this.executionContext = executionContext;
    }

    @Override
    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    @Override
    public void setConfigurationResource(Resource resource) {
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }

    public String getLabel() {
        String label =  getStringPropertyValue(RDFS.label);
        return (label != null) ? label : resource.asResource().getLocalName();
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

    public String getStringPropertyValue(@NotNull  Property property) {

        Statement st = resource.getProperty(property);
        if (st == null) {
            return null;
        }
        return resource.getProperty(property).getObject().toString();
    }

    protected RDFNode getEffectiveValue(Property valueProperty) {
        RDFNode valueNode = resource.getProperty(valueProperty).getObject();
        if (SPINExpressions.isExpression(valueNode)) {
            Resource expr = (Resource) SPINFactory.asExpression(valueNode);
            QuerySolution bindings = executionContext.getVariablesBinding().asQuerySolution();
            RDFNode node = SPINExpressions.evaluate(expr, resource.getModel(), bindings); //TODO resource.getModel() should be part o context
            if (node == null) {
                LOG.error("SPIN expression {} for bindings {} evaluated to null.", expr, bindings);
            }
            return node;
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
