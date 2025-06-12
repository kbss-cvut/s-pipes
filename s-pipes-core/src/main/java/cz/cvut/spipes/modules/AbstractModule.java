package cz.cvut.spipes.modules;

import cz.cvut.spipes.config.AuditConfig;
import cz.cvut.spipes.config.Environment;
import cz.cvut.spipes.config.ExecutionConfig;
import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.engine.VariablesBinding;
import cz.cvut.spipes.exception.ValidationConstraintFailedException;
import cz.cvut.spipes.spin.model.Ask;
import cz.cvut.spipes.spin.model.Construct;
import cz.cvut.spipes.spin.model.SPINFactory;
import cz.cvut.spipes.spin.model.Select;
import cz.cvut.spipes.manager.factory.ScriptManagerFactory;
import cz.cvut.spipes.util.JenaUtils;
import cz.cvut.spipes.util.QueryUtils;
import cz.cvut.spipes.util.SPINUtils;
import cz.cvut.spipes.util.SPipesUtil;
import org.apache.jena.atlas.lib.NotImplemented;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.RDFS;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

/**
 * The `AbstractModule` class serves as a foundational abstract class for defining
 * modules executed in a processing pipeline. Each module is responsible for
 * executing specific tasks within a given execution context, validating input and
 * output constraints, and handling module-specific configurations.
 *
 * <p>Subclasses are required to implement the {@link #executeSelf()} method, which
 * encapsulates the core logic of the module's execution. This class also provides
 * various utility methods to assist with constraint validation, model management,
 * and logging during execution.
 */
public abstract class AbstractModule implements Module {
    public static final String ID_PARAM = "_pId";
    public static final String IS_EXECUTION_OF = "isExecutionOf";
    public static final String IS_EXECUTION_OF_MODULE = "module";
    public static final String IS_EXECUTION_OF_FUNCTION = "function";



    /**
     * Adds a binding of var isExecutionOf as "module"
     * @param context
     */
    public static void setIsExecutionOfModule(ExecutionContext context){
        context.getVariablesBinding().add(
                IS_EXECUTION_OF,
                context.getDefaultModel().createLiteral(IS_EXECUTION_OF_MODULE)
        );
    }

    /**
     * Adds a binding of var isExecutionOf as "function"
     * @param context
     */
    public static void setIsExecutionOfFunction(ExecutionContext context){
        context.getVariablesBinding().add(
                IS_EXECUTION_OF,
                context.getDefaultModel().createLiteral(IS_EXECUTION_OF_FUNCTION)
        );
    }

    private static final Logger log = LoggerFactory.getLogger(AbstractModule.class);
    Resource resource;
    List<Module> inputModules = new LinkedList<>();
    ExecutionContext executionContext;
    ExecutionContext outputContext;
    private List<Resource> inputConstraintQueries;
    private List<Resource> outputConstraintQueries;
    protected boolean isInDebugMode;
    private boolean isTargetModule;


    // load each properties
    // valiadation of required parameter
    // ?? validation of shape of input graph
    // TODO move elsewhere?
    static {
        SPipesUtil.init(); // initialize system functoins
    }


    abstract ExecutionContext executeSelf();

    @Override
    public ExecutionContext execute() {
        loadModuleFlags();
        loadConfiguration();
        loadModuleConstraints();
        String inputModelFilePath = null;
        if (AuditConfig.isEnabled() || isInDebugMode) {
            inputModelFilePath = saveModelToTemporaryFile(executionContext.getDefaultModel());
            log.debug("Saving module's execution input to file {}.", inputModelFilePath);
        }
        if (ExecutionConfig.isCheckValidationConstrains()) {
            checkInputConstraints();
        }
        outputContext = executeSelf();
        if (AuditConfig.isEnabled() || isInDebugMode) {
            log.debug("Saving module's execution output to file {}.", saveModelToTemporaryFile(outputContext.getDefaultModel()));
        }

        if (ExecutionConfig.isCheckValidationConstrains()) {
            checkOutputConstraints();
        }

        if (ExecutionConfig.getEnvironment().equals(Environment.development)) {
            generateLinkToRerunExecution(inputModelFilePath);
        }

        return outputContext;
    }


    private String encodeValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     *
     * @return "module" or "function" based on weather the current module is executed as a function or as a module
     */
    public String getIsExecutionOf(){
        return Optional.ofNullable(executionContext.getVariablesBinding().getNode(IS_EXECUTION_OF))
                .map(RDFNode::toString)
                .orElse(null);
    }

    /**
     *
     * @return "module" or "function" based on weather the current module is executed as a function or as a module
     */
    public String getId(){
        return Optional.ofNullable(executionContext.getVariablesBinding().getNode(ID_PARAM))
                .map(RDFNode::toString)
                .orElse(null);
    }

    // TODO - Check if this will work in debugging! For example, when executing a module (i.e. "service/module" runModule)
    //  defined in file1 as part of a function defined in file2 (with). In such cases, this method will return the parent
    //  directory of file1 instead of correct parent directory of file2.
    //  Solutions:
    //  - To overcome this issue the the ontology iri of file2 (the one containing the function) needs to be passed to
    //  this method. May be it is ok to pass it as binding in the execution context.
    /**
     * This method returns the root script file derived from the current <code>AbstractModule.executionContext</code>.
     * If <code>AbstractModule.executionContext</code>  is a function execution (i.e. pipeline) the file the function
     * declaration is returned. If <code>AbstractModule.executionContext</code> is a module execution the file
     * containing the module declaration is retuned.
     *
     * A <code>AbstractModule.executionContext</code> is function or module execution if it contains a binding of
     * variable <code>IS_EXECUTION_OF</code> with value IS_EXECUTION_OF_FUNCTION and IS_EXECUTION_OF_MODULE respectively.
     *
     * @apiNote the caller must ensure that the binding is added before executing the pipeline or the model.
     * @apiNote Downstream modules (modules executed before this module) can alter the bindings IS_EXECUTION_OF.
     *
     * @return the root script file or null if IS_EXECUTION_OF binding is missing or incorrect
     */
    public File getRootScriptFile(){
        String id = getId();
        String isExecutionOf = getIsExecutionOf();

        String file = switch (isExecutionOf) {
            case IS_EXECUTION_OF_MODULE -> ScriptManagerFactory.getSingletonSPipesScriptManager().getModuleLocation(id, null);
            case IS_EXECUTION_OF_FUNCTION -> ScriptManagerFactory.getSingletonSPipesScriptManager().getFunctionLocation(id);
            default -> null;
        };
        return Optional.ofNullable(file).map(File::new).orElse(null);
    }

    private void generateLinkToRerunExecution(String inputModelFilePath) {
        final String SPIPES_SERVICE_URL = ExecutionConfig.getDevelopmentServiceUrl();

        String inputModelFileUrl = convertPathToURL(Optional.ofNullable(inputModelFilePath)
            .orElse(saveModelToTemporaryFile(executionContext.getDefaultModel())));
        String inputBindingFileUrl = convertPathToURL(saveModelToTemporaryFile(executionContext.getVariablesBinding().getModel()));
        String configModelFileUrl = convertPathToURL(saveScriptToTemporaryFile(this.resource.getModel()));

        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("_pId", this.resource.getURI());
        requestParams.put("_pConfigURL", configModelFileUrl);
        requestParams.put("_pInputGraphURL", inputModelFileUrl);
        requestParams.put("_pInputBindingURL", inputBindingFileUrl);

        String encodedURL = requestParams.keySet().stream()
            .map(key -> key + "=" + encodeValue(requestParams.get(key)))
            .collect(joining("&", SPIPES_SERVICE_URL + "/module?", ""));


        log.debug("To rerun the execution visit {}", encodedURL);
    }

    private String convertPathToURL (String filePath) {
        if (filePath.startsWith("/")) {
            filePath = filePath.substring(1); // removing leading '/' for UNIX file paths
        }
        return "file:///" + filePath;
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
        String label = getStringPropertyValue(RDFS.label);
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

    interface RDFModelWriter {
        void write(OutputStream outputStream, Model model);
    }

    private String saveModelToTemporaryFile(RDFModelWriter rdfModelWriter, Model model, String filePrefix) {
        File tempFile = null;
        try {
            tempFile = Files.createTempFile(filePrefix, ".ttl").toFile();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        try (OutputStream tempFileIs = new FileOutputStream(tempFile)) {
            rdfModelWriter.write(tempFileIs, model);

            return tempFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected String saveModelToTemporaryFile(Model model) {
        return saveModelToTemporaryFile(
            JenaUtils::write,
            model,
            "model-output-"
        );
    }

    protected String saveScriptToTemporaryFile(Model model) {
        return saveModelToTemporaryFile(
            JenaUtils::writeScript,
            model,
            "model-output-"
        );
    }

    protected String saveFullModelToTemporaryFile(OntModel model) {
        return saveModelToTemporaryFile(
            new RDFModelWriter() {
                @Override
                public void write(OutputStream outputStream, Model model) {
                    ((OntModel) model).writeAll(outputStream, FileUtils.langTurtle);
                }
            },
            model,
            "full-model-output-"
        );
    }

    private void loadModuleConstraints() {
        inputConstraintQueries = getResourcesByProperty(KBSS_MODULE.JENA.has_input_graph_constraint);
        outputConstraintQueries = getResourcesByProperty(KBSS_MODULE.JENA.has_output_graph_constraint);
    }

    private void loadModuleFlags() {
        isTargetModule = getPropertyValue(KBSS_MODULE.JENA.has_debug_mode_flag, false);
        isInDebugMode = getPropertyValue(KBSS_MODULE.JENA.has_debug_mode_flag, false);
    }


    private void checkOutputConstraints() {
        Model defaultModel = outputContext.getDefaultModel();

        // merge input and output execution context
        VariablesBinding mergedVarsBinding = new VariablesBinding(executionContext.getVariablesBinding().asQuerySolution());
        mergedVarsBinding.extendConsistently(outputContext.getVariablesBinding());

        if (!outputConstraintQueries.isEmpty()) {
            log.debug("Validating module's output constraints ...");
            checkConstraints(defaultModel, mergedVarsBinding.asQuerySolution(), outputConstraintQueries);
        }
    }

    void checkInputConstraints() {
        Model defaultModel = executionContext.getDefaultModel();

        QuerySolution bindings = executionContext.getVariablesBinding().asQuerySolution();

        if (!inputConstraintQueries.isEmpty()) {
            log.debug("Validating module's input constraints ...");
            checkConstraints(defaultModel, bindings, inputConstraintQueries);
        }
    }


    private void checkConstraints(Model model, QuerySolution bindings, List<Resource> constraintQueries) {

        // sort queries based on order specified by comments within query
        constraintQueries = sortConstraintQueries(constraintQueries);

        //      set up variable bindings
        for (Resource queryRes : constraintQueries) {
            cz.cvut.spipes.spin.model.Query spinQuery = SPINFactory.asQuery(queryRes);

            // TODO template call
//            if (spinQuery == null) {
//                TemplateCall templateCall = SPINFactory.asTemplateCall(queryRes);
//            }

            Query query = QueryUtils.createQuery(spinQuery);

            QueryExecution execution = QueryExecutionFactory.create(query, model, bindings);

            boolean constraintViolated;
            List<Map<String,RDFNode>> evidences = new ArrayList<>();
            if (spinQuery instanceof Ask) {
                constraintViolated = execution.execAsk();
            } else if (spinQuery instanceof Select) { //TODO implement
                ResultSet rs = execution.execSelect();
                constraintViolated = rs.hasNext();
                if(constraintViolated){

                    for(int i = 0; i < ExecutionConfig.getEvidenceNumber() && rs.hasNext(); i++){
                        QuerySolution solution = rs.next() ;
                        Map<String, RDFNode> evidenceMap = new LinkedHashMap<>();
                        for (String varName : rs.getResultVars()) {
                            RDFNode value = solution.get(varName);
                            evidenceMap.put(varName, value);
                        }
                        evidences.add(evidenceMap);
                    }
                }
            } else if (spinQuery instanceof Construct) {
                throw new NotImplemented("Constraints for objects " + query + " not implemented.");
            } else {
                throw new NotImplemented("Constraints for objects " + query + " not implemented.");
            }
            if (constraintViolated) {
                String mainErrorMsg =  getQueryComment(spinQuery);
                String failedQueryMsg = spinQuery.toString();
                var exception = new ValidationConstraintFailedException(this, mainErrorMsg, failedQueryMsg, evidences);
                log.error(exception.toString());
                if (ExecutionConfig.isExitOnError()) {
                    throw exception;
                }
            } else {
                log.debug("Constraint validated for exception \"{}\".", getQueryComment(spinQuery));
            }
        }

    }

    /**
     * Returns the query comment associated with the <code>query</code> resource argument. This is either the string
     * associated with <code>query</code> rdfs:comment or if not present the first comment line found in the query string.
     * If neither is present, the method returns the uri of the query resource.
     * @param query
     * @return
     */
    protected String getQueryComment(cz.cvut.spipes.spin.model.Query query) {
        if (query.getComment() != null) {
            return query.getComment();
        }
        String comment = QueryUtils.getQueryComment(query.toString());
        if (comment != null) {
            return comment;
        }
//        Resource obj = query.getPropertyResourceValue(RDFS.comment);
//        if (obj == null) {
//            return query.getURI();
//        }
        return query.getURI();
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

    Resource getPropertyValue(Property property, Resource defaultValue) {

        Statement s = resource.getProperty(property);

        if (s != null && s.getObject().isURIResource()) {
            return s.getResource();
        }
        return defaultValue;
    }


    boolean getPropertyValue(@NotNull Property property, boolean defaultValue) {

        Statement s = resource.getProperty(property);

        if (s != null && s.getObject().isLiteral()) {
            //TODO check if it is boolean first
            return s.getBoolean();
        }
        return defaultValue;
    }

    String getPropertyValue(@NotNull Property property, String defaultValue) {

        Statement s = resource.getProperty(property);

        if (s != null && s.getObject().isLiteral()) {
            //TODO check if it is string first
            return s.getString();
        }
        return defaultValue;
    }

    char getPropertyValue(@NotNull Property property, char defaultValue) {

        Statement s = resource.getProperty(property);

        if (s != null && s.getObject().isLiteral()) {
            return s.getObject().asLiteral().getChar();
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
        return SPINUtils.getEffectiveValue(resource, valueProperty, executionContext);
    }

    /**
     * Helper method to creates output execution context considering isReplace flag
     * indicating if newly computed model should replace input model of the module
     * or be appended to it.
     * @param isReplace if true replace input model otherwise append to it.
     * @param computedModel model to be reflected in final output of this module.
     * @return Output execution context to be returned by this module.
     */
    protected ExecutionContext createOutputContext(boolean isReplace, Model computedModel) {
        return ExecutionContextFactory.createContext(
            createOutputModel(isReplace, computedModel)
        );
    }

    /**
     * Helper method to creates output model considering isReplace flag
     * indicating if newly computed model should replace input model of the module
     * or be appended to it.
     * @param isReplace if true replace input model otherwise append to it.
     * @param computedModel model to be reflected in final output of this module.
     * @return Output model to be returned by this module.
     */
    protected Model createOutputModel(boolean isReplace, Model computedModel) {
        if (isReplace) {
            return computedModel;
        } else {
            if (AuditConfig.isEnabled() || ExecutionConfig.getEnvironment().equals(Environment.development)) {
                log.debug("Saving module's computed output to file {}.", saveModelToTemporaryFile(computedModel));
            }
            return JenaUtils.createUnion(executionContext.getDefaultModel(), computedModel);
        }
    }


    private List<Resource> sortConstraintQueries(List<Resource> constraintQueries) {
        return constraintQueries.stream().sorted((resource1, resource2) -> {
            cz.cvut.spipes.spin.model.Query spinQuery1 = SPINFactory.asQuery(resource1);
            cz.cvut.spipes.spin.model.Query spinQuery2 = SPINFactory.asQuery(resource2);

            return spinQuery1.toString().compareTo(spinQuery2.toString());
        }).collect(Collectors.toList());
    }

//    @Override
//    public String toString() {
//        String resourceId = (resource  != null) ? ( " (" + resource.getId() + ")") : "";
//        return resourceId;
//    }
}
