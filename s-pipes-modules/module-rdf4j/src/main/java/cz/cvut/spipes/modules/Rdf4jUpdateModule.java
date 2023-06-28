package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.topbraid.spin.vocabulary.SP;
import java.util.List;
import java.util.logging.Logger;

public class Rdf4jUpdateModule extends AbstractModule{
    private static final Logger logger = Logger.getLogger(Rdf4jUpdateModule.class.getName());
    private static final String TYPE_URI = KBSS_MODULE.getURI()+"rdf4j-update";
    private static final String PROPERTY_PREFIX_URI = KBSS_MODULE.getURI()+"rdf4j";
    private RepositoryConnection updateConnection;
    static final Property P_RDF4J_SERVER_URL = getParameter("p-rdf4j-server-url");
    static final Property P_RDF4J_REPOSITORY_NAME = getParameter("p-rdf4j-repository-name");
    private List<Resource> updateQueries;

    private static Property getParameter(final String name) {
        return ResourceFactory.createProperty(PROPERTY_PREFIX_URI + "/" + name);
    }
    private String getUpdateType(String updateString){
        StringBuilder res = new StringBuilder();
        char[] updateArr = updateString.toCharArray();
        for (char c : updateArr) {
            if (c == ' ') break;
            res.append(c);
        }
        return res.toString();
    }

    @Override
    ExecutionContext executeSelf() {
        for (Resource updateQuery : updateQueries) {
            String text = updateQuery.getProperty(SP.text).getLiteral().getString();
            String[] queries = text.split("\\}");
            for (int j = 0; j < queries.length - 1; j++) {
                queries[j] = (queries[j] + "}").trim();
                String updateString = queries[j];
                if( (j < queries.length-2) && (getUpdateType((queries[j + 1]+"}").trim()).equals("WHERE")) ){
                    updateString += (queries[j+1]+"}").trim();
                    j++;
                }
                makeUpdate(updateString);
            }
        }
        updateConnection.close();
        return this.executionContext;
    }

    void makeUpdate(String updateString){
        org.eclipse.rdf4j.query.Update prepareUpdate = null;
        try {
            prepareUpdate = updateConnection.prepareUpdate(QueryLanguage.SPARQL,updateString);
        }
        catch (MalformedQueryException e){
            logger.severe("Malformed Query, query text:\n"+updateString);
            return;
        }
        catch (RepositoryException e){
            logger.severe("Repository exception\n"+e.getMessage());
            return;
        }
        try {
            assert prepareUpdate != null;
            prepareUpdate.execute();
            logger.info("Update executed");
        }
        catch (UpdateExecutionException e){
            logger.severe("Update execution exception, query text:\n"+updateString+"\n"+e.getMessage());
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
            logger.info("Connection created for repository "+rdf4jRepositoryName);
        }
        catch (RepositoryException e){
            logger.severe("Repository exception\n"+e.getMessage());
        }
    }
}
