package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.util.JenaUtils;
import cz.cvut.spipes.util.QueryUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.spin.model.Construct;
import org.topbraid.spin.system.SPINModuleRegistry;
import org.topbraid.spin.vocabulary.SP;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Getter
@Setter
public abstract class ApplyConstructAbstractModule extends AnnotatedAbstractModule {

    private static final String TYPE_URI = KBSS_MODULE.uri + "abstract-apply-construct";
    private static final String PROPERTY_PREFIX_URI = KBSS_MODULE.uri;

    // TODO - this parameter is reused in ApplyConstructWithChunkedValuesAndScrollableCursorModule. There the comment should be extended by a note, i.e. "The construct queries with markers #${VALUES} and #${LIMIT_OFFSET}."
    @Parameter(iri = SML.constructQuery, comment = "SPARQL Construct query (sp:Construct)" +
        " that should be executed by this module. The query is read from sp:text property." +
        " The output of query execution is returned by the module.")
    protected List<Resource> constructQueries;

    @Parameter(iri = SML.replace, comment = "Specifies whether a module should overwrite triples" +
        " from its predecessors. When set to true (default is false), it prevents" +
        " passing through triples from the predecessors.")
    protected boolean isReplace = false;

    @Parameter(iri = KBSS_MODULE.has_max_iteration_count,
            comment =
                    "Maximal number of iterations of the whole rule set. 0 means 0 iterations. The actual number of iterations can be smaller,\n" +
                    "if no new inferences are generated any more.\n" +
                    "<p>\n" +
                    "iterationCount = 1:\n" +
                    "- the whole rule set is executed only once.\n" +
                    "iterationCount > 1:\n" +
                    "- the whole rule set is executed at most \"iterationCount\" times.\n" +
                    "- in each iteration, queries are evaluated on the model merged from the default model and the result of previous iteration\n" +
                    "<p>\n" +
                    "Within each iteration, all queries are evaluated on the same model."

    )
    protected int iterationCount = -1;

    public ApplyConstructAbstractModule() {
        // TODO move elsewhere
        SPINModuleRegistry.get().init(); //TODO -- downloads spin from the web (should be cached instead)
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
        List<Construct> constructQueriesSorted = constructQueries
            .stream().map(r -> r.as(Construct.class))
            .sorted(Comparator.comparing(this::getQueryComment))
            .toList();

        if (!shouldTerminate(0, previousInferredModel, inferredModel)) {
            while (!shouldTerminate(++count, previousInferredModel, inferredModel)) {

                Model inferredInSingleIterationModel = ModelFactory.createDefaultModel();

                log.debug("Executing iteration {} ...", count);
                QuerySolution currentIterationBindings = generateIterationBinding(count, bindings);

                for (Construct spinConstructRes : constructQueriesSorted) {

                    String queryStr = QueryUtils.getQueryWithModelPrefixes(spinConstructRes.getProperty(SP.text).getLiteral().getString(), spinConstructRes.getModel());
                    Query query = QueryFactory.create(substituteQueryMarkers(count, queryStr));

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
    public void loadManualConfiguration(){
        log.debug("Loaded {} spin construct queries.", constructQueries.size());
    }

}
