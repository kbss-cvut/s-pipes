package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.KBSS_MODULE;
import cz.cvut.sempipes.constants.SML;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.util.QueryUtils;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.mgt.Explain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.model.Construct;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.vocabulary.SP;

public abstract class  ApplyConstructAbstractModule extends AnnotatedAbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(ApplyConstructAbstractModule.class);

    //sml:constructQuery
    protected List<Resource> constructQueries;

    //sml:replace
    protected boolean isReplace;

    //kbss:parseText
    /**
     * Whether the query should be taken from sp:text property instead of from SPIN serialization
     */
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

        while (! shouldTerminate(count, previousInferredModel, inferredModel)) {
            count++;

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

                Model constructedModel = QueryUtils.execConstruct(query,
                    ModelFactory.createUnion(defaultModel, inferredModel), currentIterationBindings);

                inferredInSingleIterationModel = ModelFactory.createUnion(inferredInSingleIterationModel, constructedModel);
            }

            previousInferredModel = inferredModel;
//            inferredModel = ModelFactory.createUnion(inferredModel, inferredInSingleIterationModel);
            Model newModel = ModelFactory.createDefaultModel();
            newModel.add(inferredModel).add(inferredInSingleIterationModel);
            inferredModel = newModel;

        }

        if (isReplace) {
            return ExecutionContextFactory.createContext(inferredModel);
        } else {
            return ExecutionContextFactory.createContext(ModelFactory.createUnion(defaultModel, inferredModel));
        }
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

        LOG.debug("Loading spin constuct queries ... " + constructQueries);

        //TODO default value must be taken from template definition
        isReplace = this.getPropertyValue(SML.replace, false);

        parseText = this.getPropertyValue(KBSS_MODULE.is_parse_text, false);
        iterationCount = this.getPropertyValue(KBSS_MODULE.has_max_iteration_count, -1);
    }




}
