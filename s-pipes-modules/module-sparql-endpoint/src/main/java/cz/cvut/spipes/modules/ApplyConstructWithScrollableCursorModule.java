package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import cz.cvut.spipes.util.QueryUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * TODO Order of queries is not enforced.
 */
@Slf4j
@SPipesModule(label = "apply construct with scrollable cursor",
        comment = "Runs one or more construct queries (bound to sml:constructQuery) on the input triples. Queries are " +
                "executed multiple times with scrollable cursor that is injected through query marker #${LIMIT_OFFSET}. " +
                "The marker is replaced each time with appropriate by sparql constructs 'LIMIT ?limit' and 'OFFSET ?offset'. " +
                "Within each construct query The output RDF will consist of the constructed triples and (unless sml:replace is true) " +
                "the input triples.")
public class ApplyConstructWithScrollableCursorModule extends ApplyConstructAbstractModule {

    private static final String TYPE_URI = KBSS_MODULE.uri + "apply-construct-with-scrollable-cursor";
    private static final String TYPE_PREFIX = TYPE_URI + "/";
    private static final int DEFAULT_PAGE_SIZE = 10000;
    private static final String LIMIT_OFFSET_CLAUSE_MARKER_NAME = "LIMIT_OFFSET";
    private static final Property P_PAGE_SIZE = ResourceFactory.createProperty(TYPE_PREFIX + "page-size");

    @Parameter(iri = TYPE_PREFIX + "page-size", comment = "Page size. Default value is 10000.")
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

        log.debug("Creating query with LIMIT {} for OFFSET {}.", pageSize, offset );
        String markerValue = "\n" + "OFFSET " + offset
            + "\n" + "LIMIT " + pageSize + "\n";

        return QueryUtils
            .substituteMarkers(LIMIT_OFFSET_CLAUSE_MARKER_NAME, markerValue, queryStr);
    }


    @Override
    public void loadManualConfiguration() {
        super.loadConfiguration();
        //iterationCount = this.getPropertyValue(KBSS_MODULE.JENA.s_max_iteration_count, 1);
    }
}
