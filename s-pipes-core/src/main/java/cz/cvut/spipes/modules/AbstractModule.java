package cz.cvut.spipes.modules;

import cz.cvut.spipes.config.AuditConfig;
import cz.cvut.spipes.config.ExecutionConfig;
import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.engine.VariablesBinding;
import cz.cvut.spipes.exception.ValidationConstraintFailed;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.jena.atlas.lib.NotImplemented;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.model.Ask;
import org.topbraid.spin.model.Construct;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.model.Select;
import org.topbraid.spin.util.SPINExpressions;
import org.topbraid.spin.vocabulary.SP;

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
//        if (isInDebugMode) {
//            LOG.debug("Entering debug mode within this module.");
//        }
        loadConfiguration();
        loadModuleConstraints();
        if (ExecutionConfig.isCheckValidationConstrains()) {
            checkInputConstraints();
        }
        if (AuditConfig.isEnabled() || isInDebugMode) {
            LOG.debug("Saving module execution input to file {}.", saveModelToTemporaryFile(executionContext.getDefaultModel()));
        }
        outputContext = executeSelf();
        if (ExecutionConfig.isCheckValidationConstrains()) {
            checkOutputConstraints();
        }
        if (AuditConfig.isEnabled() || isInDebugMode) {
            LOG.debug("Saving module execution output to file {}.", saveModelToTemporaryFile(outputContext.getDefaultModel()));
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
        Model defaultModel = outputContext.getDefaultModel();

        // merge input and output execution context
        VariablesBinding mergedVarsBinding = new VariablesBinding(executionContext.getVariablesBinding().asQuerySolution());
        mergedVarsBinding.extendConsistently(outputContext.getVariablesBinding());

        if (! outputConstraintQueries.isEmpty()) {
            LOG.debug("Validating module's output constraints ...");
            checkConstraints(defaultModel, mergedVarsBinding.asQuerySolution(), outputConstraintQueries);
        }
    }

    void checkInputConstraints() {
        Model defaultModel = executionContext.getDefaultModel();

        QuerySolution bindings = executionContext.getVariablesBinding().asQuerySolution();

        if (! inputConstraintQueries.isEmpty()) {
            LOG.debug("Validating module's input constraints ...");
            checkConstraints(defaultModel, bindings, inputConstraintQueries);
        }
    }


    private void checkConstraints(Model model, QuerySolution bindings, List<Resource> constraintQueries) {

        //      set up variable bindings
        for (Resource queryRes : constraintQueries) {
            org.topbraid.spin.model.Query spinQuery = SPINFactory.asQuery(queryRes);

            // TODO template call
//            if (spinQuery == null) {
//                TemplateCall templateCall = SPINFactory.asTemplateCall(queryRes);
//            }

            Query query = ARQFactory.get().createQuery(spinQuery);

            QueryExecution execution = QueryExecutionFactory.create(query, model, bindings);

            boolean constaintViolated = false;

            if (spinQuery instanceof Ask) {
                constaintViolated = execution.execAsk();
            } else if (spinQuery instanceof Select) { //TODO implement
//                ResultSet rs = execution.execSelect();
//                constaintViolated = rs.hasNext();
//                String qs = spinQuery.getComment();
//                if (qs == null) {
//
//                }
//                String evidenceStr = "Evidence of the violation : ";
                throw new NotImplemented("Constraints for objects " + query + " not implemented.");
            } else if (spinQuery instanceof Construct) {
                throw new NotImplemented("Constraints for objects " + query + " not implemented.");
            } else {
                throw new NotImplemented("Constraints for objects " + query + " not implemented.");
            }

            if (constaintViolated) {

                String mainErrorMsg = String.format("Validation of constraint failed for the constraint \"%s\".", getQueryComment(spinQuery));
                String failedQueryMsg = String.format("Failed validation constraint : \n %s", spinQuery.toString());
                String mergedMsg = new StringBuffer()
                        .append(mainErrorMsg).append("\n")
                        .append(failedQueryMsg).append("\n")
                        .toString();
                LOG.error(mergedMsg);
                if (ExecutionConfig.isExitOnError()) {
                    throw new ValidationConstraintFailed(mergedMsg, this);
                }
            } else {
                LOG.debug("Constraint validated for exception \"{}\".", getQueryComment(spinQuery));
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
        final Statement s = resource.getProperty(property);
        return (s != null) ? s.getObject() : null;
    }

    int getPropertyValue(Property property, int defaultValue) {

        Statement s = resource.getProperty(property);

        if (s != null && s.getObject().isLiteral()) {
            //TODO check if it is boolean first
            return s.getInt();
        }
        return defaultValue;
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

    protected RDFNode getEffectiveValue(@NotNull Property valueProperty) {
        RDFNode valueNode = Optional.of(resource)
                .map(r -> r.getProperty(valueProperty))
                .map(Statement::getObject)
                .orElse(null);
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
