package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.KBSS_MODULE;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.engine.VariablesBinding;
import org.apache.jena.atlas.lib.NotImplemented;
import org.apache.jena.query.*;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.constraints.ConstraintViolation;
import org.topbraid.spin.constraints.SPINConstraints;
import org.topbraid.spin.model.*;
import org.topbraid.spin.util.SPINExpressions;
import org.topbraid.spin.vocabulary.SP;

import java.io.*;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Miroslav Blasko on 10.5.16.
 */
public abstract class AbstractModule implements Module {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractModule.class);
    Resource resource;
    List<Module> inputModules = new LinkedList<>();
    ExecutionContext executionContext;
    ExecutionContext outputContext;
    private List<Resource> inputConstraintQueries;
    private List<Resource> outputConstraintQueries;
    private boolean isInDebugMode;
    private boolean isTargetModule;


    // load each properties
    // valiadation of required parameter
    // ?? validation of shape of input graph


    abstract ExecutionContext executeSelf();

    @Override
    public ExecutionContext execute() {
        loadModuleFlags();
        if (isInDebugMode) {
            LOG.debug("Entering debug mode within this module.");
        }
        loadConfiguration();
        loadModuleConstraints();
        checkInputConstraints();
        outputContext = executeSelf();
        checkOutputConstraints();
        if (isInDebugMode) {
            LOG.debug("Saveing module execution output to file {}.", saveModelToTemporaryFile(outputContext.getDefaultModel()));
        }
        return  outputContext;
    }

    @Override
    public void addOutputBindings(VariablesBinding additionalVariablesBinding) {
        VariablesBinding mergedVarsBinding = new VariablesBinding(outputContext.getVariablesBinding().asQuerySolution());
        mergedVarsBinding.extendConsistently(additionalVariablesBinding);
        outputContext = ExecutionContextFactory.createContext(outputContext.getDefaultModel(), mergedVarsBinding);
    }


    @Override
    public ExecutionContext getOutputContext() {
        return outputContext;
    }

    @Override
    public void setInputContext(ExecutionContext executionContext) {
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


    /* ------------------ PRIVATE METHODS --------------------- */

    // TODO revise
    private String saveModelToTemporaryFile(Model model) {
        File tempFile = null;
        try {
            tempFile = Files.createTempFile("formgen-", ".ttl").toFile();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        try (OutputStream tempFileIs = new FileOutputStream(tempFile)) {
            model.write(tempFileIs, FileUtils.langTurtle);

            return tempFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadModuleConstraints() {
        inputConstraintQueries = getResourcesByProperty(KBSS_MODULE.has_input_graph_constraint);
        outputConstraintQueries = getResourcesByProperty(KBSS_MODULE.has_output_graph_constraint);
    }

    private void loadModuleFlags() {
        isTargetModule = getPropertyValue(KBSS_MODULE.has_debug_mode_flag, false);
        isInDebugMode = getPropertyValue(KBSS_MODULE.has_debug_mode_flag, false);
    }


    private void checkOutputConstraints() {

    }

    private void checkInputConstraints() {
        Model defaultModel = executionContext.getDefaultModel();

        QuerySolution bindings = executionContext.getVariablesBinding().asQuerySolution();
        Model mergedModel = ModelFactory.createDefaultModel();

        //      set up variable bindings
        for (Resource queryRes : inputConstraintQueries) {
            org.topbraid.spin.model.Query spinQuery = SPINFactory.asQuery(queryRes);

            // TODO template call
//            if (spinQuery == null) {
//                TemplateCall templateCall = SPINFactory.asTemplateCall(queryRes);
//            }

            Query query = ARQFactory.get().createQuery(spinQuery);

            QueryExecution execution = QueryExecutionFactory.create(query, defaultModel, bindings);

            boolean constaintViolated = false;
            String additionalInfo = null;

            if (spinQuery instanceof Ask) {
                constaintViolated = execution.execAsk();
            } else if (spinQuery instanceof Select) { //TODO implement
                throw new NotImplemented("Constraints for objects " + query + " not implemented.");
            } else if (spinQuery instanceof Construct) {
                throw new NotImplemented("Constraints for objects " + query + " not implemented.");
            } else {
                throw new NotImplemented("Constraints for objects " + query + " not implemented.");
            }

            if (constaintViolated) {
                LOG.error("Validation of input constraints failed -- {}.", getQueryComment(spinQuery));
                LOG.error("Failed validation constraint -- {}", spinQuery.toString());
                if (additionalInfo != null) {
                    LOG.error("Failed validation constraint info : ", additionalInfo);
                }
            }
        }
    }

    private String getQueryComment(org.topbraid.spin.model.Query query) {
        if (query.getComment() != null) {
            return query.getComment();
        }
//        Resource obj = query.getPropertyResourceValue(RDFS.comment);
//        if (obj == null) {
//            return query.getURI();
//        }
        return query.getURI();
    }

    private org.topbraid.spin.model.Query getQuery(Resource queryResource) {
        if (queryResource.hasProperty(RDF.type, SP.Ask)){
            return queryResource.as(Ask.class);
        }
        if (queryResource.hasProperty(RDF.type, SP.Construct)) {
            return queryResource.as(Construct.class);
        }
        if (queryResource.hasProperty(RDF.type, SP.Select)) {
            return queryResource.as(Select.class);
        }

        throw new IllegalStateException("Unknown query resource type -- " + queryResource.getPropertyResourceValue(RDF.type));
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

    protected String getStringPropertyValue(@NotNull Property property) {

        Statement st = resource.getProperty(property);
        if (st == null) {
            return null;
        }
        return resource.getProperty(property).getObject().toString();
    }

    protected List<Resource> getResourcesByProperty(Property property) {
        return resource.listProperties(property)
                .toList().stream()
                .map(st -> st.getObject().asResource())
                .collect(Collectors.toList());
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
