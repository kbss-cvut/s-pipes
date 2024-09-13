package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.VariablesBinding;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import cz.cvut.spipes.recursion.ChunkedValuesProvider;
import cz.cvut.spipes.recursion.CombinedQueryTemplateRecursionProvider;
import cz.cvut.spipes.recursion.QueryTemplateRecursionProvider;
import cz.cvut.spipes.recursion.ScrollableCursorProvider;
import cz.cvut.spipes.util.QueryUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.jetbrains.annotations.NotNull;
import org.topbraid.spin.model.Select;

/**
 * TODO Order of queries is not enforced.
 * TODO issue with redundant call {@link ScrollableCursorProvider}
 * TODO supports only one CONSTRUCT query
 */
@Slf4j
@SPipesModule(label = "apply construct with chunked values and scrollable cursor", comment = "Apply construct with chunked values and scrollable cursor")
public class ApplyConstructWithChunkedValuesAndScrollableCursorModule extends ApplyConstructAbstractModule {

    private static final String TYPE_URI = KBSS_MODULE.uri + "apply-construct-with-chunked-values-and-scrollable-cursor";
    private static final String TYPE_PREFIX = TYPE_URI + "/";
    private static final int DEFAULT_CHUNK_SIZE = 10;
    private static final int DEFAULT_PAGE_SIZE = 10000;
    private static final Property P_CHUNK_SIZE = ResourceFactory.createProperty(TYPE_PREFIX + "chunk-size");
    private static final Property P_PAGE_SIZE = ResourceFactory.createProperty(TYPE_PREFIX + "page-size");

    @Parameter(iri = TYPE_PREFIX + "chunk-size", comment = "Chunk size. Default is 10.")
    private Integer chunkSize = DEFAULT_CHUNK_SIZE;

    @Parameter(iri = TYPE_PREFIX + "page-size", comment = "Page size for the scrollable cursor. Default is 10000.")
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    @Parameter(iri = SML.selectQuery,
            comment = "The select query that will be used to iterate over construct query templates.")
    private Select selectQuery;


    private QueryTemplateRecursionProvider recursionProvider;

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public Select getSelectQuery() {
        return selectQuery;
    }

    public void setSelectQuery(Select selectQuery) {
        this.selectQuery = selectQuery;
    }

    private ResultSet getSelectQueryResultSet() {
        Query query = QueryUtils.createQuery(selectQuery);

        QuerySolution inputBindings = executionContext.getVariablesBinding().asQuerySolution();

        QueryExecution execution = QueryExecutionFactory.create(query, executionContext.getDefaultModel(), inputBindings);

        log.debug("Executing query of chunk provider ...");

        ResultSet selectResultSet = execution.execSelect();

        VariablesBinding variablesBinding = new VariablesBinding();

        if (!selectResultSet.hasNext()) {
            log.debug("\"{}\" query did not return any values.", getLabel());
        }
        return selectResultSet;
    }


    @Override
    protected boolean shouldTerminate(int currentIteration, Model previousIterationModel, Model currentIterationModel) {

        if (!parseText) {
            throw new IllegalArgumentException("Construct queries with SPIN notations [parseText=false] are not supported as they do not support additions of comments.");
        }

        return getRecursionProvider().shouldTerminate(currentIteration, previousIterationModel, currentIterationModel);
    }


    @Override
    protected String substituteQueryMarkers(int currentIteration, String queryStr) {

        return getRecursionProvider().substituteQueryMarkers(currentIteration, queryStr);
    }


    @Override
    public void loadConfiguration() {
        super.loadConfiguration();
        //iterationCount = this.getPropertyValue(KBSS_MODULE.JENA.s_max_iteration_count, 1);
        parseText = this.getPropertyValue(KBSS_MODULE.JENA.is_parse_text, true);
        chunkSize = this.getPropertyValue(P_CHUNK_SIZE, DEFAULT_CHUNK_SIZE);
        selectQuery = getPropertyValue(SML.JENA.selectQuery).asResource().as(Select.class);
        pageSize = this.getPropertyValue(P_PAGE_SIZE, DEFAULT_PAGE_SIZE);
    }

    @NotNull
    private QueryTemplateRecursionProvider getRecursionProvider() {
        if (recursionProvider == null) {
            ResultSet rs = getSelectQueryResultSet();
            recursionProvider = new CombinedQueryTemplateRecursionProvider(
                iterationCount,
                new ChunkedValuesProvider(rs, chunkSize, iterationCount),
                new ScrollableCursorProvider(pageSize, iterationCount)
            );
        }
        return recursionProvider;
    }

}
