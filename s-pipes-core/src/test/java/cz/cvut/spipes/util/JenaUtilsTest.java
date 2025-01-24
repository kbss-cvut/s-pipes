package cz.cvut.spipes.util;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.jena.query.Query;
import org.junit.jupiter.api.Test;

public class JenaUtilsTest {

    private static final String HASH_FILE_PREFIX = "hash-example-";

    @Test
    public void computeHash() throws Exception {

        String[] exampleIds = {"1", "2", "3", "4"};

        Map<String, String> file2ModelHashMap = Arrays.stream(exampleIds)
                .map(id -> "/util/hash-example-" + id + ".ttl")
                .collect(Collectors.toMap(
                        path -> path,
                        path -> {
                            URL fileUrl = JenaUtilsTest.class.getResource(path);
                            Model m = ModelFactory.createDefaultModel();
                            m.read(String.valueOf(fileUrl), null, FileUtils.langTurtle);
                            return JenaUtils.computeHash(m);
                        }
                ));


        Iterator<Map.Entry<String, String>> it = file2ModelHashMap.entrySet().iterator();
        Map.Entry<String, String> firstHashEntry = it.next();
        while (it.hasNext()) {
            Map.Entry<String, String> nextHashEntry = it.next();
            String errMessage = "Hashes of ontologies from files " + firstHashEntry.getKey() + " and " + nextHashEntry.getKey() + " are not same.";
            assertEquals(firstHashEntry.getValue(), nextHashEntry.getValue(), errMessage);
        }
    }

    @Test
    public void testGetQueryWithModelPrefixesReturnsAllPrefixesFromQuery() throws Exception {
        Model m = ModelFactory.createDefaultModel();

        Map<String, String> prefixMapQuery = new HashMap();
        prefixMapQuery.put("airports_from_query1", "http://onto.fel.cvut.cz/ontologies/airports_from_query1/");
        prefixMapQuery.put("airports_from_query2", "http://onto.fel.cvut.cz/ontologies/airports_from_query2/");
        prefixMapQuery.put("airports_from_query3", "http://onto.fel.cvut.cz/ontologies/airports_from_query3/");

        String prefixStr = "";
        for (Map.Entry<String, String> entry : prefixMapQuery.entrySet())
            prefixStr = prefixStr + "PREFIX " + entry.getKey() + ": <" + entry.getValue() + ">\n";

        String queryStr = QueryUtils.getQueryWithModelPrefixes(prefixStr + """
            CONSTRUCT {
              ?airport__previous airports_from_query1:is-before-airport ?airport .
            } WHERE {
            
              #${VALUES}
            }
            """, m);

        try{
            Query query = new QueryFactory().create(queryStr);
            assertEquals(query.getPrefixMapping().numPrefixes(), prefixMapQuery.size());

            for (Map.Entry<String, String> entry : prefixMapQuery.entrySet())
                assertEquals(query.getPrefixMapping().getNsPrefixURI(entry.getKey()), entry.getValue());
        }
        catch (Exception e) {
            e.printStackTrace();
            assert(false);
        }
    }

    @Test
    public void testGetQueryWithModelPrefixesReturnsAllPrefixesFromModel() throws Exception {
        Model m = ModelFactory.createDefaultModel();

        Map<String, String> prefixMapModel = new HashMap();
        prefixMapModel.put("airports_from_model1", "http://onto.fel.cvut.cz/ontologies/airports_from_model1/");
        prefixMapModel.put("airports_from_model2", "http://onto.fel.cvut.cz/ontologies/airports_from_model2/");
        prefixMapModel.put("airports_from_model3", "http://onto.fel.cvut.cz/ontologies/airports_from_model3/");

        for (Map.Entry<String, String> entry : prefixMapModel.entrySet())
            m.setNsPrefix(entry.getKey(), entry.getValue());

        String queryStr = QueryUtils.getQueryWithModelPrefixes("""
                CONSTRUCT {
                  ?airport__previous airports_from_model1:is-before-airport ?airport .
                } WHERE {

                  #${VALUES}
                }
                """, m);
        try{
            Query query = new QueryFactory().create(queryStr);
            assertEquals(query.getPrefixMapping().numPrefixes(), prefixMapModel.size());

            for (Map.Entry<String, String> entry : prefixMapModel.entrySet())
                assertEquals(query.getPrefixMapping().getNsPrefixURI(entry.getKey()), entry.getValue());
        }
        catch (Exception e) {
            e.printStackTrace();
            assert(false);
        }
    }

    @Test
    public void testGetQueryWithModelPrefixesReturnsAllPrefixesFromQueryAndModel() throws Exception {
        Model m = ModelFactory.createDefaultModel();

        Map<String, String> prefixMapQuery = new HashMap();
        prefixMapQuery.put("airports_from_query1", "http://onto.fel.cvut.cz/ontologies/airports_from_query1/");
        prefixMapQuery.put("airports_from_query2", "http://onto.fel.cvut.cz/ontologies/airports_from_query2/");
        prefixMapQuery.put("airports_from_query3", "http://onto.fel.cvut.cz/ontologies/airports_from_query3/");

        String prefixStr = "";
        for (Map.Entry<String, String> entry : prefixMapQuery.entrySet())
            prefixStr = prefixStr + "PREFIX " + entry.getKey() + ": <" + entry.getValue() + ">\n";

        Map<String, String> prefixMapModel = new HashMap();
        prefixMapModel.put("airports_from_model1", "http://onto.fel.cvut.cz/ontologies/airports_from_model1/");
        prefixMapModel.put("airports_from_model2", "http://onto.fel.cvut.cz/ontologies/airports_from_model2/");
        prefixMapModel.put("airports_from_model3", "http://onto.fel.cvut.cz/ontologies/airports_from_model3/");

        for (Map.Entry<String, String> entry : prefixMapModel.entrySet())
            m.setNsPrefix(entry.getKey(), entry.getValue());

        String queryStr = QueryUtils.getQueryWithModelPrefixes(prefixStr + """
            CONSTRUCT {
              ?airport__previous airports_from_query1:is-before-airport ?airport .
              ?airport__previous airports_from_model1:is-before-airport ?airport .
            } WHERE {
            
              #${VALUES}
            }
            """, m);

        try{
            Query query = new QueryFactory().create(queryStr);
            assertEquals(query.getPrefixMapping().numPrefixes(), prefixMapQuery.size() + prefixMapModel.size());

            for (Map.Entry<String, String> entry : prefixMapQuery.entrySet())
                assertEquals(query.getPrefixMapping().getNsPrefixURI(entry.getKey()), entry.getValue());

            for (Map.Entry<String, String> entry : prefixMapModel.entrySet())
                assertEquals(query.getPrefixMapping().getNsPrefixURI(entry.getKey()), entry.getValue());
        }
        catch (Exception e) {
            e.printStackTrace();
            assert(false);
        }
    }
}