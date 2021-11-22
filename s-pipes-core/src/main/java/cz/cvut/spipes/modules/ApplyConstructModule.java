package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
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

/**
 * TODO Order of queries is not enforced.   
 *
 **/
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
            Model extendedInferredModel = JenaUtils.createUnion(defaultModel, inferredModel);

            for (int i = 0; i < constructQueries.size(); i++) {
                Construct spinConstructRes = constructQueries.get(i).as(Construct.class);

                if (LOG.isTraceEnabled()) {
                    String queryComment = getQueryComment(spinConstructRes);;
                    LOG.trace(
                        "Executing iteration {}/{} with {}/{} query \"{}\" ...",
                        count, iterationCount, i + 1, constructQueries.size(), queryComment
                    );
                }

                Query query;
                if (parseText) {
                    query = QueryFactory.create(spinConstructRes.getProperty(SP.text).getLiteral().getString());
                } else {
                    query = ARQFactory.get().createQuery(spinConstructRes);
                }

                Model constructedModel = QueryUtils.execConstruct(query, extendedInferredModel, bindings);

                if (LOG.isTraceEnabled()) {
                    LOG.trace("... the query returned {} triples.", constructedModel.size());
                }

                if (isInDebugMode) {
                    LOG.debug("... saving module partially computed output to file {}.", saveModelToTemporaryFile(constructedModel));
                }

                inferredInSingleIterationModel = ModelFactory.createUnion(inferredInSingleIterationModel, constructedModel);
            }

            inferredModel = JenaUtils.createUnion(inferredModel, inferredInSingleIterationModel);

            nNew = inferredInSingleIterationModel.size();
        }

        return this.createOutputContext(isReplace, defaultModel, inferredModel);
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
