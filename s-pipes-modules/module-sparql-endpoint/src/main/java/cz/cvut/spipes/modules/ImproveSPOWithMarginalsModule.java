package cz.cvut.spipes.modules;

import cz.cvut.spipes.ModelLogger;
import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.engine.VariablesBinding;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import cz.cvut.spipes.tdb.TDBTempFactory;
import cz.cvut.spipes.util.JenaUtils;
import cz.cvut.spipes.util.QueryUtils;
import static cz.cvut.spipes.util.VariableBindingUtils.restrict;
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

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.FileUtils;
import org.jetbrains.annotations.NotNull;

/**
 * TODO Order of queries is not enforced.
 */
@Slf4j
@SPipesModule(label = "improve spo with marginals", comment = "Constructs improved spo-summary descriptor with knowledge " +
        "of provided marginals of weakly described resources. This module expects as an input graph computed spo-summary " +
        "patterns (or possibly whole spo-summary descriptor) compliant with data provided in ?data-service-url. Within the " +
        "input graph it identifies 'breakable patterns', i.e. spo-summary patterns that can be improved with knowledge of " +
        "marginals computed in ?marginals-defs-file-url. The output of the module is a spo-summary descriptor that  contains " +
        "original spo-summary patterns whenever possible and new spo-summary patterns that were created with additional " +
        "knowledge of marginals.")
public class ImproveSPOWithMarginalsModule extends AnnotatedAbstractModule {

    private static final String MODULE_ID = "improve-spo-with-marginals";
    private static final String TYPE_URI = KBSS_MODULE.uri + MODULE_ID;
    private static final String TYPE_PREFIX = TYPE_URI + "/";
    private static final Map<String, Model> marginalDefsModelCache = new HashMap<>();

    @Parameter(iri = TYPE_PREFIX + "marginal-constraint", comment = "Marginal constraint")
    private String marginalConstraint;
    @Parameter(iri = TYPE_PREFIX + "marginals-defs-file-url", comment = "Marginal definitions file url")
    private String marginalsDefsFileUrl;
    @Parameter(iri = TYPE_PREFIX + "marginals-file-url", comment = "Marginals file url") // TODO - review comment
    private String marginalsFileUrl;
    @Parameter(iri = TYPE_PREFIX + "data-service-url", comment = "Data service url")
    private String dataServiceUrl;

    private static final String VAR_EXECUTION_ID = "executionId";

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
        final ModelLogger mLOG = new ModelLogger(MODULE_ID, log);

        final Model inputModel = executionContext.getDefaultModel();
        final VariablesBinding inputVB = executionContext.getVariablesBinding();

        log.debug("Retrieving relevant snapshots ...");
        // TODO it should be taken from parameter somehow
        Model relevantSnapshotsModel = retrieveRelevantSnapshots(executionContext.getVariablesBinding());
        mLOG.trace("relevant-snapshots", relevantSnapshotsModel);

        log.debug("Loading marginals ...");
        Model marginalsModel = loadModelFromFile(marginalsFileUrl);
        mLOG.trace("marginals", marginalsModel);

        log.debug("Loading marginal definitions ...");
        Model marginalDefsModel = loadModelFromFile(marginalsDefsFileUrl);
        mLOG.trace("marginal-defs", marginalDefsModel);

        Model marginalsWithDefsModel = ModelFactory.createUnion(
            relevantSnapshotsModel,
            ModelFactory.createUnion(marginalsModel, marginalDefsModel)
        );

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

            QuerySolution bp = breakablePatternsRS.next();
            if (!bp.get("pattern").toString().equals("http://onto.fel.cvut.cz/ontologies/dataset-descriptor/spo-summary/di-b364a27fa33296eb3bb27bfa9b3cab9c")) {
                continue;
            }

            // get pattern data
            Model patternDataModel = getPatternData(breakablePatternsRS, spoPatternDataQueryTemplate);
            mLOG.trace("pattern-data-" + i, patternDataModel);

            // extract appropriate marginals
            Model marginalTypesModel = computeMarginalTypesModel(patternDataModel, marginalsWithDefsModel);
            mLOG.trace("marginal-types-" + i, marginalTypesModel);

            Model spoPatternDataWithMarginalsModel = ModelFactory.createUnion(patternDataModel, marginalTypesModel);
            mLOG.trace("pattern-data-with-marginals-" + i, spoPatternDataWithMarginalsModel);

            Model spoWithWeight = computeSPOWithWeight(
                spoPatternDataWithMarginalsModel,
                restrict(inputVB, VAR_EXECUTION_ID)
            );
            mLOG.trace("spo-pattern-with-weight-" + i, spoWithWeight);

            Model spoWithSnapshots = computeSPOWithSnapshots(
                spoPatternDataWithMarginalsModel,
                restrict(inputVB, VAR_EXECUTION_ID)
            );
            mLOG.trace("spo-pattern-with-snapshosts-" + i, spoWithSnapshots);

            Model brokenPatternModel = ModelFactory.createUnion(spoWithWeight, spoWithSnapshots);
            mLOG.trace("broken-pattern-" + i, brokenPatternModel);

            brakedPatternsOutputModel.add(brokenPatternModel);
        }

        Model nonBreakablePatternsModel = getNonBreakablePatterns(inputModel);
        mLOG.trace("non-breakable-patterns", nonBreakablePatternsModel);

        Model mergedPatternsModel = mergePatterns(ModelFactory.createUnion(brakedPatternsOutputModel, nonBreakablePatternsModel));
        mLOG.trace("merged-patterns", mergedPatternsModel);

        Model dataSourcesModel = getDatasources(mergedPatternsModel);
        mLOG.trace("datasources", dataSourcesModel);

        Model outputModel = JenaUtils.createUnion(mergedPatternsModel, dataSourcesModel);
        return ExecutionContextFactory.createContext(outputModel);
    }

    private Model retrieveRelevantSnapshots(VariablesBinding variablesBinding) {
        return QueryUtils.execConstruct(
            loadQueryFromFile("/get-relevant-snapshots.rq"),
            ModelFactory.createDefaultModel(),
            variablesBinding.asQuerySolution()
        );
    }

    private Model getDatasources(Model spoModel) {
        return QueryUtils.execConstruct(
            loadQueryFromFile("/get-datasources.rq"),
            spoModel,
            new QuerySolutionMap()
        );
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
        log.debug("Executing query to download patterns data with values: \n{}", valuesStr);
        String patternDataQueryStr = QueryUtils.substituteMarkers("VALUES", valuesStr, spoPatternDataQueryTemplate);

        Query patternDataQuery = parseQuery(patternDataQueryStr);
        VariablesBinding variablesBinding = new VariablesBinding("dataServiceUrl", ResourceFactory.createResource(dataServiceUrl));
        log.trace("Pattern data query:\n" + patternDataQueryStr);
        log.trace("Variables bindings:" + variablesBinding);
        Model patternDataModel = TDBTempFactory.createTDBModel();
        patternDataModel = QueryUtils.execConstruct(// TODO !? implement scrollable cursor
            patternDataQuery,
            ModelFactory.createDefaultModel(),
            variablesBinding.asQuerySolution(),
            patternDataModel);
        return patternDataModel;
    }


    private Model computeSPOWithWeight(Model spoPatternDataWithMarginalsModel, VariablesBinding variablesBinding) {
        log.debug("Computing SPO with weight for pattern data with marginals ...");
        Model spoModel = QueryUtils.execConstruct(
            loadQueryFromFile("/compute-spo-with-weight.rq"),
            spoPatternDataWithMarginalsModel,
            variablesBinding.asQuerySolution()
        );
        return spoModel;
    }

    private Model computeSPOWithSnapshots(Model spoPatternDataWithMarginalsModel, VariablesBinding variablesBinding) {
        log.debug("Computing SPO with snapshots for pattern data with marginals ...");
        Model spoModel = QueryUtils.execConstruct(
            loadQueryFromFile("/compute-spo-with-snapshots.rq"),
            spoPatternDataWithMarginalsModel,
            variablesBinding.asQuerySolution()
        );
        return spoModel;
    }

    private Model computeMarginalTypesModel(Model patternDataModel, Model marginalWithDefsModel) {
        log.debug("Executing query to get typed marginals ...");
        Model marginalTypesModel = QueryUtils.execConstruct(
            loadQueryFromFile("/get-marginal-types.rq"),
            ModelFactory.createUnion(patternDataModel, marginalWithDefsModel),
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
            cachedModel = TDBTempFactory.createTDBModel();
            cachedModel.read(marginalsFilePath);
            marginalDefsModelCache.put(key, cachedModel);
        } else {
            log.debug("Using cached model of file {}", marginalsFilePath);
        }
        return cachedModel;
    }

    private String computeFileContentHashKey(String filePath) {
        try {
            return filePath + " -- " + Files.getLastModifiedTime(Paths.get(URI.create(filePath))).toMillis();
        } catch (IOException e) {
            log.warn("Could not access file from path " + filePath + ": " + e);
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
            log.debug("Saving model to temporary file " + filePath + " ...");
            JenaUtils.write(new FileOutputStream(filePath), model);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

    @Override
    public void loadManualConfiguration() {
        super.loadManualConfiguration();
    }

    private @NotNull
    String getEffectiveStringValue(String propertyUrl) {
        RDFNode value = getEffectiveValue(ResourceFactory.createProperty(propertyUrl));
        if (value == null) {
            throw new RuntimeException(
                String.format("Module's parameter '%s' returned value 'null' which is not allowed.", propertyUrl)
            );
        }
        return getEffectiveValue(ResourceFactory.createProperty(propertyUrl)).asLiteral().toString();
    }
}
