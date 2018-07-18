package cz.cvut.sempipes.recursion;

import cz.cvut.sempipes.util.QueryUtils;
import java.util.Objects;
import java.util.Optional;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChunkedValuesProvider implements QueryTemplateRecursionProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ChunkedValuesProvider.class);
    private static final String VALUES_CLAUSE_MARKER_NAME = "VALUES";
    private final int iterationCount;
    private ResultSet inputResultSet;
    private Integer outputChunkSize;


    public ChunkedValuesProvider(ResultSet inputResultSet, Integer outputChunkSize, Integer iterationCount) {
        Objects.nonNull(inputResultSet);
        this.inputResultSet = inputResultSet;
        this.outputChunkSize = outputChunkSize;
        this.iterationCount = Optional.ofNullable(iterationCount).orElse(-1);
    }


    @Override
    public String substituteQueryMarkers(int currentIteration, String queryStr) {
        String markerValue = QueryUtils.nextResultsToValuesClause(inputResultSet, outputChunkSize);

        LOG.debug("Creating query with values clause: \n{}.", markerValue);

        return QueryUtils
            .substituteMarkers(VALUES_CLAUSE_MARKER_NAME,
                "\n" + markerValue + "\n",
                queryStr);

    }

    @Override
    public boolean shouldTerminate(int currentIteration, Model previousInferredModel, Model currentInferredModel) {
        if (currentIteration == iterationCount) {
            return true;
        }

        if (!inputResultSet.hasNext()) {
            return true;
        }

        return false;
    }


}
