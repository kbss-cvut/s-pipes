package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.ModelLogger;
import cz.cvut.sempipes.constants.KBSS_MODULE;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.engine.VariablesBinding;
import cz.cvut.sempipes.util.QueryUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO Order of queries is not enforced.
 */
public class ImproveSPOWithMarginalsModule extends AnnotatedAbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(ImproveSPOWithMarginalsModule.class);

    private static final String MODULE_ID = "improve-spo-with-marginals";
    private static final String TYPE_URI = KBSS_MODULE.uri + MODULE_ID;
    private static final String TYPE_PREFIX = TYPE_URI + "/";
    private static Map<String, Model> marginalDefsModelCache = new HashMap<>();
    //@Parameter(urlPrefix = TYPE_PREFIX, name = "marginal-constraint")
    private String marginalConstraint;
    //@Parameter(urlPrefix = TYPE_PREFIX, name = "marginals-defs-file-url")
    private String marginalsDefsFileUrl;
    //@Parameter(urlPrefix = TYPE_PREFIX, name = "data-service-url")
    private String dataServiceUrl;

    private static Query parseQuery(String queryStr) {
        Query query = QueryFactory.create();
        return QueryFactory.parse(
            query,
            queryStr,
            "",
            Syntax.syntaxSPARQL_11);
    }

    private static Model loadModelFromTemporaryFile(String fileName) {
        String filePath = "/home/blcha/projects/gacr/git/16gacr-model/descriptor/marginals/" + fileName;
        try {
            return ModelFactory.createDefaultModel().read(new FileInputStream(filePath), "", FileUtils.langTurtle);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    @Override
    ExecutionContext executeSelf() {
        final ModelLogger mLOG = new ModelLogger(MODULE_ID, LOG);

        final Model inputModel = executionContext.getDefaultModel();

        LOG.debug("Loading marginal definitions ...");
        Model marginalDefsModel = loadModelFromFile(marginalsDefsFileUrl);
        mLOG.trace("marginal-defs", marginalDefsModel);

        String spoPatternDataQueryTemplate = loadQueryStringFromFile("/get-spo-pattern-data.rq");
        spoPatternDataQueryTemplate = QueryUtils.substituteMarkers("MARGINAL_CONSTRAINT", marginalConstraint, spoPatternDataQueryTemplate);

        ResultSet breakablePatternsRS = QueryUtils.execSelect(
            loadQueryFromFile("/get-breakable-patterns.rq"),
            inputModel,
            new QuerySolutionMap()
        );

        Model brakedPatternsOutputModel = ModelFactory.createDefaultModel();

        int i = 0;
        while (breakablePatternsRS.hasNext()) { // TODO !? bindings -> group of bindings

            i++;

            // get pattern data
            Model patternDataModel = getPatternData(breakablePatternsRS, spoPatternDataQueryTemplate);
            mLOG.trace("pattern-data-" + i, patternDataModel);

            // extract appropriate marginals
            Model marginalTypesModel = computeMarginalTypesModel(patternDataModel, marginalDefsModel);
            mLOG.trace("marginal-types-" + i, marginalTypesModel);

            Model spoPatternDataWithMarginalsModel = ModelFactory.createUnion(patternDataModel, marginalTypesModel);
            mLOG.trace("pattern-data-with-marginals", spoPatternDataWithMarginalsModel);

            Model brakedPatternModel = computeSPO(spoPatternDataWithMarginalsModel);
            mLOG.trace("braked-pattern-" + i, brakedPatternModel);

            brakedPatternsOutputModel.add(brakedPatternModel);
        }

        Model nonBreakablePatternsModel = getNonBreakablePatterns(inputModel);
        mLOG.trace("non-breakable-patterns", nonBreakablePatternsModel);

        Model outputModel = mergePatterns(ModelFactory.createUnion(brakedPatternsOutputModel, nonBreakablePatternsModel));
        return ExecutionContextFactory.createContext(outputModel);
    }

    private Model mergePatterns(Model patternsModel) {
        return QueryUtils.execConstruct(
            loadQueryFromFile("/merge-spo-patterns.rq"),
            patternsModel,
            new QuerySolutionMap()
        );
    }

    private Model getNonBreakablePatterns(Model patternsModel) {
        return QueryUtils.execConstruct(
            loadQueryFromFile("/get-non-breakable-patterns.rq"),
            patternsModel,
            new QuerySolutionMap()
        );
    }

    private Model getPatternData(ResultSet breakablePatternsRS, String spoPatternDataQueryTemplate) {

        // substitute values
        String valuesStr = QueryUtils.nextResultsToValuesClause(breakablePatternsRS, 1);
        LOG.debug("Executing query to download patterns data with values: \n{}", valuesStr);
        String patternDataQueryStr = QueryUtils.substituteMarkers("VALUES", valuesStr, spoPatternDataQueryTemplate);

        Query patternDataQuery = parseQuery(patternDataQueryStr);
        VariablesBinding variablesBinding = new VariablesBinding("dataServiceUrl", ResourceFactory.createResource(dataServiceUrl));
        LOG.trace("Pattern data query:\n" + patternDataQueryStr);
        LOG.trace("Variables bindings:" + variablesBinding);
        Model patternDataModel = QueryUtils.execConstruct(// TODO !? implement scrollable cursor
            patternDataQuery,
            ModelFactory.createDefaultModel(),
            variablesBinding.asQuerySolution());
        return patternDataModel;
    }

    private Model computeSPO(Model spoPatternDataWithMarginalsModel) {
        LOG.debug("Computing SPO for pattern data with marginals ...");
        Model spoModel = QueryUtils.execConstruct(
            loadQueryFromFile("/compute-spo.rq"),
            spoPatternDataWithMarginalsModel,
            new QuerySolutionMap()
        );
        return spoModel;
    }

    private Model computeMarginalTypesModel(Model patternDataModel, Model marginalDefsModel) {
        LOG.debug("Executing query to get typed marginals ...");
        Model marginalTypesModel = QueryUtils.execConstruct(
            loadQueryFromFile("/get-marginal-types.rq"),
            ModelFactory.createUnion(patternDataModel, marginalDefsModel),
            new QuerySolutionMap()
        );
        return marginalTypesModel;
    }

    private Map<String, Set<String>> buildMarginal2TypeMap(Model marginalDefsModel) {
        ResultSet rs = QueryUtils.execSelect(
            loadQueryFromFile("/get-marginal-types-simple.rq"),
            marginalDefsModel,
            null);
        Map<String, Set<String>> map = new HashMap<>();
        rs.forEachRemaining(
            r -> {
                Set<String> typeSet = map.putIfAbsent(r.getResource("resource").getURI(), new HashSet<>());
                typeSet.add(r.getResource("resourceType").getURI());
            }
        );
        return map;
    }

    public String getMarginalConstraint() {
        return marginalConstraint;
    }

    public void setMarginalConstraint(String marginalConstraint) {
        this.marginalConstraint = marginalConstraint;
    }

    public String getMarginalsDefsFileUrl() {
        return marginalsDefsFileUrl;
    }

    public void setMarginalsDefsFileUrl(String marginalsDefsFileUrl) {
        this.marginalsDefsFileUrl = marginalsDefsFileUrl;
    }

    public String getDataServiceUrl() {
        return dataServiceUrl;
    }

    public void setDataServiceUrl(String dataServiceUrl) {
        this.dataServiceUrl = dataServiceUrl;
    }

    // TODO fix ad-hoc cache through static field
    private Model loadModelFromFile(String marginalsFilePath) {
        // clear cache if too big
        if (marginalDefsModelCache.size() > 10) {
            marginalDefsModelCache.clear();
        }

        String key = computeFileContentHashKey(marginalsFilePath);
        Model cachedModel = marginalDefsModelCache.get(key);

        if (cachedModel == null) {
            cachedModel = ModelFactory.createDefaultModel();
            cachedModel.read(marginalsFilePath);
            marginalDefsModelCache.put(key, cachedModel);
        } else {
            LOG.debug("Using cached model of file {}", marginalsFilePath);
        }
        return cachedModel;
    }

    private String computeFileContentHashKey(String filePath) {
        try {
            return filePath + " -- " + Files.getLastModifiedTime(Paths.get(URI.create(filePath))).toMillis();
        } catch (IOException e) {
            LOG.warn("Could not access file from path " + filePath + ": " + e);
            throw new IllegalArgumentException("Could not access modification time of file from path " + filePath, e);
        }
    }

    private String loadQueryStringFromFile(String resourcePath) {
        String queryString;
        try {
            queryString = FileUtils.readWholeFileAsUTF8(
                getClass().getResourceAsStream(resourcePath));
        } catch (IOException e) {
            throw new IllegalStateException("Could not load query from resource path " + resourcePath, e);
        }
        return queryString;
    }

    private Query loadQueryFromFile(String resourcePath) {
        Query query = QueryFactory.create();
        try {
            QueryFactory.parse(
                query,
                loadQueryStringFromFile(resourcePath),
                "",
                Syntax.syntaxSPARQL_11);
        } catch (QueryParseException e) {
            throw new IllegalStateException("Could not parse query from resource path " + resourcePath, e);
        }
        return query;
    }

    private void filterRelevantMarginalDefs() {

    }

    private String getFilePrefix() {
        String id = dataServiceUrl.replaceAll("^https://", "s-").replaceAll("^http://", "").replaceAll("[^a-zA-Z0-9-]", "_");
        String directoryPath = "/home/blcha/projects/gacr/git/16gacr-model/descriptor/marginals/" + id;

        File directory = new File(directoryPath);
        directory.mkdir();
        return directoryPath + '/';
    }

    private void saveModelToTemporaryFile(Model model, String fileName) {
        String filePath = getFilePrefix() + fileName;
        try {
            LOG.debug("Saving model to temporary file " + filePath + " ...");
            model.write(new FileOutputStream(filePath), FileUtils.langTurtle);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

    @Override
    public void loadConfiguration() {
        super.loadConfiguration();
        marginalConstraint = getEffectiveStringValue(TYPE_PREFIX + "marginal-constraint");
        marginalsDefsFileUrl = getEffectiveStringValue(TYPE_PREFIX + "marginals-defs-file-url");
        dataServiceUrl = getEffectiveStringValue(TYPE_PREFIX + "data-service-url");
    }

    private String getEffectiveStringValue(String propertyUrl) {
        return getEffectiveValue(ResourceFactory.createProperty(propertyUrl)).asLiteral().toString();
    }
}
