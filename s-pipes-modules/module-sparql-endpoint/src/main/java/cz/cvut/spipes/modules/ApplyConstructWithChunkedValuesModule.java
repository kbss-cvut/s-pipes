package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.VariablesBinding;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import cz.cvut.spipes.util.QueryUtils;
import java.util.Objects;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.model.Select;

/**
 * TODO Order of queries is not enforced.
 */
@SPipesModule(label = "apply construct with chunked values", comment = "Apply construct with chunked values.")
public class ApplyConstructWithChunkedValuesModule extends ApplyConstructAbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(ApplyConstructWithChunkedValuesModule.class);

    private static final String TYPE_URI = KBSS_MODULE.uri + "apply-construct-with-chunked-values";
    private static final String TYPE_PREFIX = TYPE_URI + "/";
    private static final int DEFAULT_CHUNK_SIZE = 10;
    private static final String VALUES_CLAUSE_MARKER_NAME = "VALUES";
    private static final Property P_CHUNK_SIZE = ResourceFactory.createProperty(TYPE_PREFIX + "chunk-size");

    @Parameter(urlPrefix = TYPE_PREFIX, name = "chunk-size")
    private Integer chunkSize = DEFAULT_CHUNK_SIZE;

    @Parameter(urlPrefix = SML.uri, name = "selectQuery")
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
        Query query = ARQFactory.get().createQuery(selectQuery);

        QuerySolution inputBindings = executionContext.getVariablesBinding().asQuerySolution();

        QueryExecution execution = QueryExecutionFactory.create(query, executionContext.getDefaultModel(), inputBindings);

        LOG.debug("Executing query of chunk provider ...");

        selectResultSet = execution.execSelect();

        VariablesBinding variablesBinding = new VariablesBinding();

        if (! selectResultSet.hasNext()) {
            LOG.debug("\"{}\" query did not return any values.", getLabel());
        }
    }



    @Override
    protected boolean shouldTerminate(int currentIteration, Model previousInferredModel, Model currentInferredModel) {

        if (!parseText) {
            throw new IllegalArgumentException("Construct queries with SPIN notations [parseText=false] are not supported as they do not support additions of comments.");
        }

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

        LOG.debug("Creating query with values clause: \n{}.", markerValue );

        return QueryUtils
            .substituteMarkers(VALUES_CLAUSE_MARKER_NAME,
                "\n" + markerValue + "\n",
                queryStr);
    }


    @Override
    public void loadConfiguration() {
        super.loadConfiguration();
        //iterationCount = this.getPropertyValue(KBSS_MODULE.has_max_iteration_count, 1);
        parseText = this.getPropertyValue(KBSS_MODULE.is_parse_text, true);
        chunkSize = this.getPropertyValue(P_CHUNK_SIZE, DEFAULT_CHUNK_SIZE);
        selectQuery = getPropertyValue(SML.selectQuery).asResource().as(Select.class);
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
