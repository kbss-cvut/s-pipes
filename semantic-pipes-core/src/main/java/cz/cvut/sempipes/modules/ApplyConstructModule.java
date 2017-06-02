package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.KBSS_MODULE;
import cz.cvut.sempipes.constants.SML;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
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

/**
 * TODO Order of queries is not enforced.   
 *
 * Created by blcha on 10.5.16.
 */
public class ApplyConstructModule extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(ApplyConstructModule.class);

    //sml:constructQuery
    private List<Resource> constructQueries;

    //sml:replace
    private boolean isReplace;

    //kbss:parseText
    /**
     * Whether the query should be taken from sp:text property instead of from SPIN serialization
     */
    private boolean parseText;

    //kbss:iterationCount
    /**
     * Maximal number of iterations of the whole rule set. 0 means 0 iterations. The actual number of iterations can be smaller,
     * if no new inferences are generated any more.
     *
     * iterationCount = 1:
     *    - the whole rule set is executed only once.
     * iterationCount > 1:
     *    - the whole rule set is executed at most "iterationCount" times.
     *    - in each iteration, queries are evaluated on the model merged from the default model and the result of previous iteration
     *
     * Within each iteration, all queries are evaluated on the same model.
     *
     */
    private int iterationCount;

    public ApplyConstructModule() {
        // TODO move elsewhere
        SPINModuleRegistry.get().init(); //TODO -- downloads spin from the web (should be cached instead)
    }

    public boolean isParseText() {
        return parseText;
    }

    public void setParseText(boolean parseText) {
        this.parseText = parseText;
    }

    public int getIterationCount() {
        return iterationCount;
    }

    public void setIterationCount(int iterationCount) {
        this.iterationCount = iterationCount;
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

    public ExecutionContext executeSelf() {

        Model defaultModel = executionContext.getDefaultModel();

        QuerySolution bindings = executionContext.getVariablesBinding().asQuerySolution();

        long nNew = 1;

        int count = 0;

        Model inferredModel = ModelFactory.createDefaultModel();

        while (nNew > 0 && count++ < iterationCount) {
            //      set up variable bindings

            Model inferredInSingleIterationModel = ModelFactory.createDefaultModel();

            for (Resource constructQueryRes : constructQueries) {
                Construct spinConstructRes = constructQueryRes.as(Construct.class);

                Query query;
                if (parseText) {
                    query = QueryFactory.create(spinConstructRes.getProperty(SP.text).getLiteral().getString());
                } else {
                    query = ARQFactory.get().createQuery(spinConstructRes);
                }

                Model constructedModel = execConstruct(query, ModelFactory.createUnion(defaultModel, inferredModel), bindings);

                inferredInSingleIterationModel = ModelFactory.createUnion(inferredInSingleIterationModel, constructedModel);
            }

            inferredModel = ModelFactory.createUnion(inferredModel, inferredInSingleIterationModel);

            nNew = inferredInSingleIterationModel.size();
        }

        if (isReplace) {
            return ExecutionContextFactory.createContext(inferredModel);
        } else {
            return ExecutionContextFactory.createContext(ModelFactory.createUnion(defaultModel, inferredModel));
        }
    }

    // TODO move this to external utils
    private Model execConstruct(Query query, Model model, QuerySolution bindings) {
        try {
            return execConstruct(query, model, bindings, false);
        } catch (RuntimeException ex) {
            LOG.error("Failed execution of query [1] for binding [2], due to exception [3]. " +
                    "The query [1] will be executed again with detailed logging turned on. " +
                    "\n\t - query [1]: \"\n{}\n\"" +
                    "\n\t - binding [2]: \"\n{}\n\"" +
                    "\n\t - exception [3]: \"\n{}\n\""
                    , query, bindings, getStackTrace(ex));
        }
        LOG.error("Executing query [1] again to diagnose the cause ...");
        return execConstruct(query, model, bindings, true);
    }

    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private Model execConstruct(Query query, Model model, QuerySolution bindings, boolean isDebugEnabled) {
        QueryExecution execution = QueryExecutionFactory.create(query,
                model, bindings);

        if (isDebugEnabled) {
            execution.getContext().set(ARQ.symLogExec, Explain.InfoLevel.ALL);
        }
        return execution.execConstruct();
    }

    @Override
    public String getTypeURI() {
        return SML.ApplyConstruct.getURI();
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
        iterationCount = this.getPropertyValue(KBSS_MODULE.has_max_iteration_count, 1);
    }
}
