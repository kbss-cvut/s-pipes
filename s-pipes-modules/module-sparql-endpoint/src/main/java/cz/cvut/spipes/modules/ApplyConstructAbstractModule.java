package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import cz.cvut.spipes.util.JenaUtils;
import cz.cvut.spipes.util.QueryUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.model.Construct;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.vocabulary.SP;

import java.util.List;

@SPipesModule(label = "abstract apply construct", comment = "abstract module defining properties for subclasses of apply construct.")
public abstract class ApplyConstructAbstractModule extends AnnotatedAbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(ApplyConstructAbstractModule.class);
    private static final String TYPE_URI = KBSS_MODULE.uri + "abstract-apply-construct";
    private static final String PROPERTY_PREFIX_URI = KBSS_MODULE.uri + "";
    //sml:constructQuery
    @Parameter(urlPrefix = SML.uri, name = "constructQuery")
    protected List<Resource> constructQueries;

    //sml:replace
    @Parameter(urlPrefix = SML.uri, name = "replace")
    protected boolean isReplace;

    //kbss:parseText
    /**
     * Whether the query should be taken from sp:text property instead of from SPIN serialization
     */
    @Parameter(name = "is-parse-text")
    protected boolean parseText;

    //kbss:iterationCount
    /**
     * Maximal number of iterations of the whole rule set. 0 means 0 iterations. The actual number of iterations can be smaller,
     * if no new inferences are generated any more.
     * <p>
     * iterationCount = 1:
     * - the whole rule set is executed only once.
     * iterationCount > 1:
     * - the whole rule set is executed at most "iterationCount" times.
     * - in each iteration, queries are evaluated on the model merged from the default model and the result of previous iteration
     * <p>
     * Within each iteration, all queries are evaluated on the same model.
     */
    @Parameter(name = "has-max-iteration-count")
    protected int iterationCount = -1;


    public ApplyConstructAbstractModule() {
        // TODO move elsewhere
        SPINModuleRegistry.get().init(); //TODO -- downloads spin from the web (should be cached instead)
    }

    public boolean isParseText() {
        return parseText;
    }

    public void setParseText(boolean parseText) {
        this.parseText = parseText;
    }

    public boolean isReplace() {
        return isReplace;
    }

    public void setReplace(boolean replace) {
        isReplace = replace;
    }

    public List<Resource> getConstructQueries() {
        return constructQueries;
    }

    public void setConstructQueries(List<Resource> constructQueries) {
        this.constructQueries = constructQueries;
    }

    public int getIterationCount() {
        return iterationCount;
    }

    public void setIterationCount(int iterationCount) {
        this.iterationCount = iterationCount;
    }


    protected QuerySolution generateIterationBinding(int currentIteration, QuerySolution globalBinding) {
        return globalBinding;
    }

    protected boolean shouldTerminate(int currentIteration, Model previousInferredModel, Model currentInferredModel) {

        if (currentIteration == iterationCount) {
            return true;
        }

        if ((currentIteration > 0) && (previousInferredModel.size() == currentInferredModel.size())) {
            return true;
        }

        return false;
    }

    public abstract String getTypeURI();


    public ExecutionContext executeSelf() {

        Model defaultModel = executionContext.getDefaultModel();

        QuerySolution bindings = executionContext.getVariablesBinding().asQuerySolution();

        int count = 0;

        Model inferredModel = ModelFactory.createDefaultModel();
        Model previousInferredModel = ModelFactory.createDefaultModel();

        if (!shouldTerminate(0, previousInferredModel, inferredModel)) {
            while (!shouldTerminate(++count, previousInferredModel, inferredModel)) {

                Model inferredInSingleIterationModel = ModelFactory.createDefaultModel();


                QuerySolution currentIterationBindings = generateIterationBinding(count, bindings);

                for (Resource constructQueryRes : constructQueries) {
                    Construct spinConstructRes = constructQueryRes.as(Construct.class);

                    Query query;
                    if (parseText) {
                        String queryStr = spinConstructRes.getProperty(SP.text).getLiteral().getString();
                        query = QueryFactory.create(substituteQueryMarkers(count, queryStr));
                    } else {
                        query = ARQFactory.get().createQuery(spinConstructRes);
                    }

                    Model constructedModel = QueryUtils.execConstruct(
                        query,
                        JenaUtils.createUnion(defaultModel, inferredModel),
                        currentIterationBindings
                    );

                    inferredInSingleIterationModel = JenaUtils.createUnion(inferredInSingleIterationModel, constructedModel);
                }

                previousInferredModel = inferredModel;
                inferredModel = JenaUtils.createUnion(inferredModel, inferredInSingleIterationModel);
            }
        }

        return createOutputContext(isReplace, inferredModel);
    }

    protected String substituteQueryMarkers(int currentIteration, String queryStr) {
        return queryStr;
    }

    @Override
    public void loadConfiguration() {

        // TODO sparql expressions
        // TODO load default values from configuration

        // TODO does not work with string query as object is not RDF resource ???
        constructQueries = getResourcesByProperty(SML.constructQuery);

        LOG.debug("Loaded {} spin construct queries.", constructQueries.size());

        //TODO default value must be taken from template definition
        isReplace = this.getPropertyValue(SML.replace, false);

        parseText = this.getPropertyValue(KBSS_MODULE.is_parse_text, false);
        iterationCount = this.getPropertyValue(KBSS_MODULE.has_max_iteration_count, -1);
    }


}
