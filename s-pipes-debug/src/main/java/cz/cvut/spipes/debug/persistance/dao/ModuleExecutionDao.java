package cz.cvut.spipes.debug.persistance.dao;

import static cz.cvut.kbss.jopa.model.JOPAPersistenceProperties.ONTOLOGY_PHYSICAL_URI_KEY;

import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.springframework.stereotype.Repository;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.spipes.model.ModuleExecution;

@Repository
public class ModuleExecutionDao extends AbstractDao<ModuleExecution> {

    protected ModuleExecutionDao(EntityManager em) {
        super(em);
    }

    public Boolean askContainOutput(String context, String graphPattern) {
        try {
            return (Boolean) em.createNativeQuery("ASK {"
                    + "  GRAPH <" + context + "> {"
                    + graphPattern +
                    "  }}").getSingleResult();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public Boolean askContainInputAndNotContainOutput(String inputContext, String outputContext, String graphPattern) {
        try {
            return (Boolean) em.createNativeQuery(String.format("ASK {"
                            + "  GRAPH <%s> {"
                            + "    FILTER NOT EXISTS {%s}"
                            + "  }"
                            + "  GRAPH <%s> {%s}"
                            + "}", outputContext, graphPattern, inputContext, graphPattern))
                    .getSingleResult();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public Set<Statement> getModelForOutputContext(String context) {
        String query = String.format("SELECT ?s ?p ?o { GRAPH <%s> { ?s ?p ?o}}", context);
        return executeSelectQuery(query);
    }

    public Set<Statement> executeSelectQuery(String queryString) {
        Map<String, Object> properties = em.getProperties();
        String url = (String) properties.get(ONTOLOGY_PHYSICAL_URI_KEY);
        org.eclipse.rdf4j.repository.Repository repository = new HTTPRepository(url);
        RepositoryConnection connection = repository.getConnection();
        TupleQuery tupleQuery = connection.prepareTupleQuery(queryString);
        TupleQueryResult result = tupleQuery.evaluate();
        return getModelFromTupleQueryResult(result);
    }

    private Set<Statement> getModelFromTupleQueryResult(TupleQueryResult result) {
        Model model = new LinkedHashModel();
        while (result.hasNext()) {
            BindingSet bindingSet = result.next();
            Statement statement = extractStatementFromBindingSet(bindingSet);
            model.add(statement);
        }
        return model;
    }

    private Statement extractStatementFromBindingSet(BindingSet bindingSet) {
        String subject = bindingSet.getValue("s").stringValue();
        String predicate = bindingSet.getValue("p").stringValue();
        Value object = bindingSet.getValue("o");

        ValueFactory valueFactory = SimpleValueFactory.getInstance();
        IRI subjectIRI = valueFactory.createIRI(subject);
        IRI predicateIRI = valueFactory.createIRI(predicate);
        Value objectValue = createAppropriateValue(valueFactory, object);

        return valueFactory.createStatement(subjectIRI, predicateIRI, objectValue);
    }

    private Value createAppropriateValue(ValueFactory valueFactory, Value value) {
        if (value instanceof IRI) {
            return value;
        } else if (value instanceof Literal) {
            return value;
        } else {
            return valueFactory.createLiteral(value.stringValue());
        }
    }
}