package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import cz.cvut.spipes.util.QueryUtils;
import cz.cvut.spipes.util.query.OneStepBackExtendedResultSet;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.jetbrains.annotations.NotNull;
import org.topbraid.spin.model.Select;

import java.util.Objects;

/**
 * Apply construct query with chunked values.
 * The construct query is provided as a template with marker `#${VALUES}`.
 * The marker is substituted with chunked values from a select query.
 *
 * <p>Example select query:</p>
 * <pre>
 * SELECT ?person ?lastName
 * WHERE {
 *   ?person a foaf:Person .
 *   ?person foaf:lastName ?lastName .
 * }
 * </pre>
 *
 * <p>Example construct query:</p>
 * <pre>
 * CONSTRUCT {
 *   ?person a :User .
 *   ?person :has-last-name ?lastName .
 * }
 * WHERE {
 *   #${VALUES}
 *   FILTER(strlen(?lastName) < 20)
 * }
 * </pre>
 */
@Slf4j
@SPipesModule(label = "apply construct with chunked values", comment = "Apply construct with chunked values.")
public class ApplyConstructWithChunkedValuesModule extends ApplyConstructAbstractModule {

    private static final String TYPE_URI = KBSS_MODULE.uri + "apply-construct-with-chunked-values";
    private static final String TYPE_PREFIX = TYPE_URI + "/";
    private static final int DEFAULT_CHUNK_SIZE = 10;
    private static final String VALUES_CLAUSE_MARKER_NAME = "VALUES";
    private static final Property P_CHUNK_SIZE = ResourceFactory.createProperty(TYPE_PREFIX + "chunk-size");

    @Parameter(iri = TYPE_PREFIX + "chunk-size", comment = "Chunk size. Default is 10.")
    private Integer chunkSize = DEFAULT_CHUNK_SIZE;

    @Parameter(iri = SML.selectQuery,
        comment = "The select query that will be used to iterate over construct query templates.")
    private Select selectQuery;


    private ResultSet selectResultSet;

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

    public void initializeQuery() {
        Query query = QueryUtils.createQuery(selectQuery);

        QuerySolution inputBindings = executionContext.getVariablesBinding().asQuerySolution();

        QueryExecution execution = QueryExecutionFactory.create(query, executionContext.getDefaultModel(), inputBindings);

        log.debug("Executing query of chunk provider ...");

        selectResultSet = new OneStepBackExtendedResultSet(execution.execSelect());

        if (! selectResultSet.hasNext()) {
            log.debug("\"{}\" query did not return any values.", getLabel());
        }
    }



    @Override
    protected boolean shouldTerminate(int currentIteration, Model previousInferredModel, Model currentInferredModel) {

        if (currentIteration == iterationCount) {
            return true;
        }

        if (! getCurrentResultSetInstance().hasNext()) {
            return true;
        }

        return false;
    }


    @Override
    protected String substituteQueryMarkers(int currentIteration, String queryStr) {

        String markerValue = QueryUtils.nextResultsToValuesClause(getCurrentResultSetInstance(), chunkSize);

        log.trace("Creating query with values clause: \n{}.", markerValue );

        return QueryUtils
            .substituteMarkers(VALUES_CLAUSE_MARKER_NAME,
                "\n" + markerValue + "\n",
                queryStr);
    }


    @Override
    public void loadManualConfiguration() {
        super.loadManualConfiguration();
        //iterationCount = this.getPropertyValue(KBSS_MODULE.JENA.s_max_iteration_count, 1);
    }

    @NotNull
    private ResultSet getCurrentResultSetInstance() {
        if (selectResultSet == null) {
            initializeQuery();
        }
        Objects.nonNull(selectResultSet);
        return selectResultSet;
    }

}
