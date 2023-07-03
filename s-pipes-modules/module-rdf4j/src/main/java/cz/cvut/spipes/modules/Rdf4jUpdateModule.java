package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
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
import org.topbraid.spin.vocabulary.SP;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Rdf4jUpdateModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(Rdf4jUpdateModule.class.getName());
    private static final String TYPE_URI = KBSS_MODULE.getURI() + "rdf4j-update";
    private static final String PROPERTY_PREFIX_URI = KBSS_MODULE.getURI() + "rdf4j";
    private RepositoryConnection updateConnection;

    /**
     * URL of the Rdf4j server
     */
    static final Property P_RDF4J_SERVER_URL = getParameter("p-rdf4j-server-url");
    private String rdf4jServerURL;

    /**
     * Rdf4j repository ID
     */
    static final Property P_RDF4J_REPOSITORY_NAME = getParameter("p-rdf4j-repository-name");
    private String rdf4jRepositoryName;

    public String getRdf4jServerURL() {
        return rdf4jServerURL;
    }

    public void setRdf4jServerURL(String rdf4jServerURL) {
        this.rdf4jServerURL = rdf4jServerURL;
    }

    public String getRdf4jRepositoryName() {
        return rdf4jRepositoryName;
    }

    public void setRdf4jRepositoryName(String rdf4jRepositoryName) {
        this.rdf4jRepositoryName = rdf4jRepositoryName;
    }

    private List<Resource> updateQueries;

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
        for (Resource updateQueryResource : updateQueries) {
            String updateQuery = updateQueryResource.getProperty(SP.text).getLiteral().getString();
            makeUpdate(updateQuery);
        }
        updateConnection.close();
        return this.executionContext;
    }

    void makeUpdate(String updateString) {
        Update prepareUpdate = null;
        try {
            prepareUpdate = updateConnection.prepareUpdate(QueryLanguage.SPARQL, updateString);
        } catch (MalformedQueryException e) {
            logger.error("Malformed Query, query text:\n" + updateString);
            return;
        } catch (RepositoryException e) {
            logger.error("Repository exception\n" + e.getMessage());
            return;
        }
        try {
            assert prepareUpdate != null;
            prepareUpdate.execute();
            logger.debug("Update successful");
        } catch (UpdateExecutionException e) {
            logger.error("Update execution exception, query text:\n" + updateString + "\n" + e.getMessage());
        }
    }

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

    @Override
    public void loadConfiguration() {
        String rdf4jServerURL = getEffectiveValue(P_RDF4J_SERVER_URL).asLiteral().getString();
        String rdf4jRepositoryName = getEffectiveValue(P_RDF4J_REPOSITORY_NAME).asLiteral().getString();
        Repository updateRepository = new SPARQLRepository(rdf4jServerURL + "repositories/" + rdf4jRepositoryName + "/statements");
        updateQueries = getResourcesByProperty(SML.updateQuery);
        try {
            updateConnection = updateRepository.getConnection();
            logger.debug("Connected to " + rdf4jRepositoryName);
        } catch (RepositoryException e) {
            logger.error("Repository exception\n" + e.getMessage());
        }
    }
}
