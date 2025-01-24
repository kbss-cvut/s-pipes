package cz.cvut.spipes.util;

import java.net.URL;
import java.util.Arrays;
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
    public void testGetQueryWithModelPrefixesFromQuery() throws Exception {
        Model m = ModelFactory.createDefaultModel();
        String queryStr = QueryUtils.getQueryWithModelPrefixes("""
            PREFIX airports_from_query: <http://onto.fel.cvut.cz/ontologies/airports_from_query/>
            CONSTRUCT {
              ?airport__previous airports_from_query:is-before-airport ?airport .
            } WHERE {
            
              #${VALUES}
            }
            """, m);

        try{
            Query query = new QueryFactory().create(queryStr);
            assertNotNull(query);
        }
        catch (Exception e) {
            e.printStackTrace();
            assert(false);
        }
    }

    @Test
    public void testGetQueryWithModelPrefixesFromModel() throws Exception {
        Model m = ModelFactory.createDefaultModel();
        m.setNsPrefix("airports_from_model", "http://onto.fel.cvut.cz/ontologies/airports_from_model/");
        String queryStr = QueryUtils.getQueryWithModelPrefixes("""
                CONSTRUCT {
                  ?airport__previous airports_from_model:is-before-airport ?airport .
                } WHERE {
                            
                  #${VALUES}
                }
                """, m);
        try{
            Query query = new QueryFactory().create(queryStr);
            assertNotNull(query);
        }
        catch (Exception e) {
            e.printStackTrace();
            assert(false);
        }
    }
}