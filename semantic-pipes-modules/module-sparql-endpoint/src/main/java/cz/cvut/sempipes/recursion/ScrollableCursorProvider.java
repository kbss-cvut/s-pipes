package cz.cvut.sempipes.recursion;

import cz.cvut.sempipes.util.QueryUtils;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO !!! scrolling detects one iteration later than it supposed to. If there is a knowledge about how many results a subquery with LIMIT+OFFSET returned, it would be more clear ...
 */
public class ScrollableCursorProvider implements QueryTemplateRecursionProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ScrollableCursorProvider.class);
    private static final String LIMIT_OFFSET_CLAUSE_MARKER_NAME = "LIMIT_OFFSET";
    private final int pageSize;
    private final int iterationCount;


    public ScrollableCursorProvider(int pageSize, int iterationCount) {
        this.pageSize = pageSize;
        this.iterationCount = iterationCount;
    }

    @Override
    public boolean shouldTerminate(int currentIteration, Model previousInferredModel, Model currentInferredModel) {

        if (currentIteration == iterationCount) {
            return true;
        }

        if (currentIteration == 1) {
            return false;
        }

        if ((currentIteration > 0) && (previousInferredModel.size() == currentInferredModel.size())) {
            return true;
        }

        return false;
    }

    @Override
    public String substituteQueryMarkers(int currentIteration, String queryStr) {
        int offset = pageSize * (currentIteration - 1);

        LOG.debug("Creating query with LIMIT {} for OFFSET {}.", pageSize, offset );
        String markerValue = "\n" + "OFFSET " + offset
            + "\n" + "LIMIT " + pageSize + "\n";

        return QueryUtils
            .substituteMarkers(LIMIT_OFFSET_CLAUSE_MARKER_NAME, markerValue, queryStr);
    }
}
