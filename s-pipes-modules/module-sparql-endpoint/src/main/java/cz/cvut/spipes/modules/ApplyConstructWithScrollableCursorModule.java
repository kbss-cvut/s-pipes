package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import cz.cvut.spipes.util.QueryUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO Order of queries is not enforced.
 */
@SPipesModule(label = "apply construct with scrollable cursor", comment = "Apply construct with scrollable cursor.")
public class ApplyConstructWithScrollableCursorModule extends ApplyConstructAbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(ApplyConstructWithScrollableCursorModule.class);

    private static final String TYPE_URI = KBSS_MODULE.uri + "apply-construct-with-scrollable-cursor";
    private static final String TYPE_PREFIX = TYPE_URI + "/";
    private static final int DEFAULT_PAGE_SIZE = 10000;
    private static final String LIMIT_OFFSET_CLAUSE_MARKER_NAME = "LIMIT_OFFSET";
    private static final Property P_PAGE_SIZE = ResourceFactory.createProperty(TYPE_PREFIX + "page-size");

    @Parameter(urlPrefix = TYPE_PREFIX, name = "page-size")
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }



    @Override
    protected boolean shouldTerminate(int currentIteration, Model previousInferredModel, Model currentInferredModel) {

        if (!parseText) {
            throw new IllegalArgumentException("Construct queries with SPIN notations [parseText=false] are not supported as they do not support additions of comments.");
        }

        if (currentIteration == iterationCount) {
            return true;
        }

        if ((currentIteration > 1) && (previousInferredModel.size() == currentInferredModel.size())) {
            return true;
        }

        return false;
    }

    @Override
    protected String substituteQueryMarkers(int currentIteration, String queryStr) {
        int offset = pageSize * (currentIteration - 1);

        LOG.debug("Creating query with LIMIT {} for OFFSET {}.", pageSize, offset );
        String markerValue = "\n" + "OFFSET " + offset
            + "\n" + "LIMIT " + pageSize + "\n";

        return QueryUtils
            .substituteMarkers(LIMIT_OFFSET_CLAUSE_MARKER_NAME, markerValue, queryStr);
    }


    @Override
    public void loadConfiguration() {
        super.loadConfiguration();
        //iterationCount = this.getPropertyValue(KBSS_MODULE.has_max_iteration_count, 1);
        parseText = this.getPropertyValue(KBSS_MODULE.is_parse_text, true);
        pageSize = this.getPropertyValue(P_PAGE_SIZE , DEFAULT_PAGE_SIZE);
    }
}
