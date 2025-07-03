package cz.cvut.spipes.modules;

import cz.cvut.spipes.config.AuditConfig;
import cz.cvut.spipes.config.Environment;
import cz.cvut.spipes.config.ExecutionConfig;
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
import cz.cvut.spipes.spin.model.Construct;
import cz.cvut.spipes.spin.vocabulary.SP;

import java.util.Comparator;
import java.util.List;

/**
 * TODO Order of queries is not enforced.
 **/
public class ApplyConstructModule extends AnnotatedAbstractModule {

    private static final Logger log = LoggerFactory.getLogger(ApplyConstructModule.class);

    @Parameter(iri = SML.constructQuery)
    private List<Resource> constructQueries;

    @Parameter(iri = SML.replace)
    private boolean isReplace = false;

    /**
     * Whether the query should be taken from sp:text property instead of from SPIN serialization
     */
    @Parameter(iri = KBSS_MODULE.is_parse_text)
    private boolean parseText = false;

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
    @Parameter(iri = KBSS_MODULE.has_max_iteration_count)
    private int iterationCount = 1;

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

        long inferredTriplesCount = 1;

        int count = 0;

        Model inferredModel = ModelFactory.createDefaultModel();

        List<Construct> constructQueriesSorted = constructQueries
            .stream().map(r -> r.as(Construct.class))
            .sorted(Comparator.comparing(this::getQueryComment))
            .toList();

        while (inferredTriplesCount > 0 && count++ < iterationCount) {
            //      set up variable bindings

            Model inferredInSingleIterationModel = ModelFactory.createDefaultModel();
            Model extendedInferredModel = JenaUtils.createUnion(defaultModel, inferredModel);

            for (int i = 0; i < constructQueriesSorted.size(); i++) {
                Construct spinConstructRes = constructQueriesSorted.get(i);

                if (log.isTraceEnabled()) {
                    String queryComment = getQueryComment(spinConstructRes);
                    log.trace(
                        "Executing iteration {}/{} with {}/{} query \"{}\" ...",
                        count, iterationCount, i + 1, constructQueriesSorted.size(), queryComment
                    );
                }

                Query query;
                if (parseText) {
                    query = QueryFactory.create(spinConstructRes.getProperty(SP.text).getLiteral().getString());
                } else {
                    query = QueryUtils.createQuery(spinConstructRes);
                }

                Model constructedModel = QueryUtils.execConstruct(query, extendedInferredModel, bindings);

                if (log.isTraceEnabled()) {
                    log.trace("... the query returned {} triples.", constructedModel.size());
                }

                if (AuditConfig.isEnabled() || ExecutionConfig.getEnvironment().equals(Environment.development)) {
                    log.debug("... saving module's partially computed output to file {}.", saveModelToTemporaryFile(constructedModel));
                }

                inferredInSingleIterationModel = ModelFactory.createUnion(inferredInSingleIterationModel, constructedModel);
            }

            Model newModel = JenaUtils.createUnion(inferredModel, inferredInSingleIterationModel);

            inferredTriplesCount = newModel.size() - inferredModel.size();
            log.trace("Iteration {}/{} inferred {} new triples.", count, iterationCount, inferredTriplesCount);

            inferredModel = newModel;
        }

        return this.createOutputContext(isReplace, inferredModel);
    }

    @Override
    public String getTypeURI() {
        return SML.ApplyConstruct;
    }

}
