package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.exception.ModuleConfigurationInconsistentException;
import cz.cvut.spipes.exceptions.RepositoryAccessException;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import cz.cvut.spipes.util.QueryUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.Update;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.vocabulary.SP;

import java.util.List;
import java.util.stream.Collectors;

@SPipesModule(label = "rdf4j update", comment = "Updates sparql endpoint configured in rdf4jServerURL using specified updateQueries.")
public class Rdf4jUpdateModule extends AbstractModule {
    private static final Logger LOG = LoggerFactory.getLogger(Rdf4jUpdateModule.class.getName());
    private static final String TYPE_URI = KBSS_MODULE.uri + "rdf4j-update";
    private static final String PROPERTY_PREFIX_URI = KBSS_MODULE.uri + "rdf4j";

    static final Property P_RDF4J_SERVER_URL = getParameter("p-rdf4j-server-url");
    @Parameter(urlPrefix = PROPERTY_PREFIX_URI + "/", name = "p-rdf4j-server-url", comment = "URL of the Rdf4j server")
    private String rdf4jServerURL;

    static final Property P_RDF4J_REPOSITORY_NAME = getParameter("p-rdf4j-repository-name");

    @Parameter(urlPrefix = PROPERTY_PREFIX_URI + "/", name = "p-rdf4j-repository-name", comment = "Rdf4j repository ID")
    private String rdf4jRepositoryName;

    @Parameter(urlPrefix = SML.uri, name = "updateQuery", comment = "A list of SPARQL Update queries. Queries strings are read from sp:text.")// TODO - revise comment
    private List<String> updateQueries;

    static final Property P_RDF4J_STOP_ITERATION_ON_STABLE_TRIPLE_COUNT =
        getParameter("p-stop-iteration-on-stable-triple-count");

    @Parameter(urlPrefix = PROPERTY_PREFIX_URI + "/", name = "p-stop-iteration-on-stable-triple-count",
            comment = "Stops iteration on if triple count did not change. Default is false.") // TODO - revise comment
    private boolean onlyIfTripleCountChanges;

    @Parameter(urlPrefix = PROPERTY_PREFIX_URI + "/", name = "has-max-iteration-count",
            comment = "Limits the number of iterations to the specified value. Default value is 1.") // TODO - revise comment
    private int iterationCount;

    private Repository updateRepository;

    public void setUpdateQueries(List<String> updateQueries) {
        this.updateQueries = updateQueries;
    }

    public List<String> getUpdateQueries() {
        return updateQueries;
    }

    public int getIterationCount() {
        return iterationCount;
    }

    public void setIterationCount(int iterationCount) {
        this.iterationCount = iterationCount;
    }

    public String getRdf4jServerURL() {
        return rdf4jServerURL;
    }

    public void setRdf4jServerURL(String rdf4jServerURL) {
        this.rdf4jServerURL = rdf4jServerURL;
    }

    public boolean isOnlyIfTripleCountChanges() {
        return onlyIfTripleCountChanges;
    }

    public void setOnlyIfTripleCountChanges(boolean onlyIfTripleCountChanges) {
        this.onlyIfTripleCountChanges = onlyIfTripleCountChanges;
    }

    public String getRdf4jRepositoryName() {
        return rdf4jRepositoryName;
    }

    public void setRdf4jRepositoryName(String rdf4jRepositoryName) {
        this.rdf4jRepositoryName = rdf4jRepositoryName;
    }

    void setUpdateRepository(Repository updateRepository) {
        this.updateRepository = updateRepository;
    }

    public static Resource createUpdateQueryResource(Model model, String updateQuery) {
        return
            model.createResource()
                .addProperty(RDF.type, SML.updateQuery)
                .addProperty(SP.text, ResourceFactory.createPlainLiteral(updateQuery));
    }

    private static Property getParameter(final String name) {
        return ResourceFactory.createProperty(PROPERTY_PREFIX_URI + "/" + name);
    }

    @Override
    ExecutionContext executeSelf() {
        try (RepositoryConnection updateConnection = updateRepository.getConnection()) {
            LOG.debug("Connected to {}", rdf4jRepositoryName);
            long newTriplesCount = updateConnection.size();
            long oldTriplesCount;
            LOG.debug("Number of triples before execution of updates: {}", newTriplesCount);

            for(int i = 0;i < iterationCount; i++) {
                oldTriplesCount = newTriplesCount;
                for (int j = 0; j < updateQueries.size(); j++) {
                    String updateQuery = updateQueries.get(j);

                    if (LOG.isTraceEnabled()) {
                        String queryComment = QueryUtils.getQueryComment(updateQuery);
                        LOG.trace(
                            "Executing iteration {}/{} with {}/{} query \"{}\" ...",
                            i+1, iterationCount, j + 1, updateQueries.size(), queryComment
                        );
                    }
                    makeUpdate(updateQuery, updateConnection);
                }
                newTriplesCount = updateConnection.size();
                LOG.debug("Number of triples after finishing iteration {}/{}: {}",
                    i+1, iterationCount, newTriplesCount
                );
                if (onlyIfTripleCountChanges && (newTriplesCount == oldTriplesCount)) {
                    LOG.debug("Stopping execution of iterations as triples count did not change.");
                    break;
                }
            }
        } catch (RepositoryException e) {
            throw new RepositoryAccessException(rdf4jRepositoryName, e);
        }

        return this.executionContext;
    }

    void makeUpdate(String updateString, RepositoryConnection updateConnection) {
        Update prepareUpdate;
        try {
            prepareUpdate = updateConnection.prepareUpdate(QueryLanguage.SPARQL, updateString);
        } catch (MalformedQueryException e) {
            LOG.error("Malformed Query, query text:\n{}",
                    updateString);
            return;
        } catch (RepositoryException e) {
            LOG.error("Repository exception\n{}",
                    e.getMessage());
            return;
        }
        try {
            assert prepareUpdate != null;
            prepareUpdate.execute();
            LOG.debug("Update successful");
        } catch (UpdateExecutionException e) {
            LOG.error("Update execution exception, query text:\n{}\n{}",
                    updateString,
                    e.getMessage());
        }
    }

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

    @Override
    public void loadConfiguration() {
        rdf4jServerURL = getEffectiveValue(P_RDF4J_SERVER_URL).asLiteral().getString();
        rdf4jRepositoryName = getEffectiveValue(P_RDF4J_REPOSITORY_NAME).asLiteral().getString();
        iterationCount = getPropertyValue(KBSS_MODULE.has_max_iteration_count,1);
        onlyIfTripleCountChanges = getPropertyValue(P_RDF4J_STOP_ITERATION_ON_STABLE_TRIPLE_COUNT,false);
        LOG.debug("Iteration count={}\nOnlyIf...Changes={}"
                ,iterationCount
                ,onlyIfTripleCountChanges);
        if (updateRepository != null && rdf4jServerURL != null) {
            throw new ModuleConfigurationInconsistentException(
                "Repository is already initialized. Trying to override its configuration from RDF.");
        }
        updateRepository = new SPARQLRepository(
            rdf4jServerURL + "/repositories/" + rdf4jRepositoryName + "/statements"
        );
        updateQueries = loadUpdateQueries();
    }

    private List<String> loadUpdateQueries() {
        return getResourcesByProperty(SML.updateQuery).stream().map(
            r -> r.getProperty(SP.text).getLiteral().getString()).collect(Collectors.toList());
    }
}
