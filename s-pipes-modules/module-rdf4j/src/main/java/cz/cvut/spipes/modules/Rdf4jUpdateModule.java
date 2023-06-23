package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.engine.ExecutionContext;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


public class Rdf4jUpdateModule extends AbstractModule{
    private static final Logger logger = Logger.getLogger(Rdf4jUpdateModule.class.getName());
    private static final String TYPE_URI = KBSS_MODULE.getURI()+"rdf4j-update";
    private static final String PROPERTY_PREFIX_URI = KBSS_MODULE.getURI()+"rdf4j";
    Repository queryRepository = new SPARQLRepository("http://localhost:8080/rdf4j-server/repositories/1");
    Repository updateRepository = new SPARQLRepository("http://localhost:8080/rdf4j-server/repositories/1/statements");

    private static void setLogger(){
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        FileHandler fh;
        boolean dirCreated = new File("./LOGS/").mkdirs();
        try {
            fh = new FileHandler("./LOGS/testlogs.txt");
        }catch (IOException e){
            return;
        }
        fh.setFormatter(new SimpleFormatter());
        logger.addHandler(fh);
    }

    @Override
    ExecutionContext executeSelf() {
        setLogger();
        logger.info("executeSelf entered");
        insertQuery();
        selectQuery("SELECT* WHERE{ ?s ?p ?o }");
        return this.executionContext;
    }

    void selectQuery(String query){
        RepositoryConnection connection = queryRepository.getConnection();
        TupleQuery tupleQuery = connection.prepareTupleQuery(query);
        TupleQueryResult tupleQueryResult = tupleQuery.evaluate();
        while(tupleQueryResult.hasNext()){
            System.out.println(tupleQueryResult.next());
        }
    }

    void insertQuery(){
        RepositoryConnection connection = updateRepository.getConnection();
        IRI john = Values.iri("http://example.org/people/john");
        IRI name = Values.iri("http://example.org/ontology/name");
        Literal johnName = Values.literal("John");
        connection.add(john,name,johnName);
    }

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

    @Override
    public void loadConfiguration() {

    }
}
