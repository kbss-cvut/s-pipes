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

        Map<String, String> prefixMapQuery = createExampleQueryPrefixMap();

        String prefixStr = createPrefixDeclarationStringFromMap(prefixMapQuery);

        String queryStr = QueryUtils.getQueryWithModelPrefixes(prefixStr + """
            CONSTRUCT {
             ?p a query-prefix1:Person .
            }
            WHERE {
              ?p a query-prefix1:Person .
            }
            """, m);

        try{
            Query query = new QueryFactory().create(queryStr);
            assertEquals(query.getPrefixMapping().numPrefixes(), prefixMapQuery.size(),
                    "Created query does not contain correct amount of prefixes");

            for (Map.Entry<String, String> entry : prefixMapQuery.entrySet())
                assertEquals(query.getPrefixMapping().getNsPrefixURI(entry.getKey()), entry.getValue(),
                        "Created query does not contain prefix from the predefined query");
        }
        catch (Exception e) {
            assert false: "Query is not parsable. Got an exception: " + e.getMessage();
        }
    }

    @Test
    public void testGetQueryWithModelPrefixesReturnsAllPrefixesFromModel() throws Exception {
        Model m = ModelFactory.createDefaultModel();

        Map<String, String> prefixMapModel = createExampleModelPrefixMap();

        for (Map.Entry<String, String> entry : prefixMapModel.entrySet())
            m.setNsPrefix(entry.getKey(), entry.getValue());

        String queryStr = QueryUtils.getQueryWithModelPrefixes("""
                CONSTRUCT {
                 ?p a model-prefix1:Person .
                }
                WHERE {
                  ?p a model-prefix1:Person .
                }
                """, m);
        try{
            Query query = new QueryFactory().create(queryStr);
            assertEquals(query.getPrefixMapping().numPrefixes(), prefixMapModel.size(),
                    "Created query does not contain correct amount of prefixes");

            for (Map.Entry<String, String> entry : prefixMapModel.entrySet())
                assertEquals(query.getPrefixMapping().getNsPrefixURI(entry.getKey()), entry.getValue(),
                        "Created query does not contain prefix from the predefined model");
        }
        catch (Exception e) {
            assert false: "Query is not parsable. Got an exception: " + e.getMessage();
        }
    }

    @Test
    public void testGetQueryWithModelPrefixesReturnsAllPrefixesFromQueryAndModel() throws Exception {
        Model m = ModelFactory.createDefaultModel();

        Map<String, String> prefixMapQuery = createExampleQueryPrefixMap();

        String prefixStr = createPrefixDeclarationStringFromMap(prefixMapQuery);

        Map<String, String> prefixMapModel = createExampleModelPrefixMap();

        for (Map.Entry<String, String> entry : prefixMapModel.entrySet())
            m.setNsPrefix(entry.getKey(), entry.getValue());

        String queryStr = QueryUtils.getQueryWithModelPrefixes(prefixStr + """
                CONSTRUCT {
                 ?p a model-prefix1:Person .
                }
                WHERE {
                  ?p a query-prefix1:Person .
                }
            """, m);

        try{
            Query query = new QueryFactory().create(queryStr);
            assertEquals(query.getPrefixMapping().numPrefixes(), prefixMapQuery.size() + prefixMapModel.size(),
                    "Created query does not contain correct amount of prefixes");

            for (Map.Entry<String, String> entry : prefixMapQuery.entrySet())
                assertEquals(query.getPrefixMapping().getNsPrefixURI(entry.getKey()), entry.getValue(),
                        "Created query does not contain prefix from the predefined query");

            for (Map.Entry<String, String> entry : prefixMapModel.entrySet())
                assertEquals(query.getPrefixMapping().getNsPrefixURI(entry.getKey()), entry.getValue(),
                        "Created query does not contain prefix from the predefined model");
        }
        catch (Exception e) {
            assert false: "Query is not parsable. Got an exception: " + e.getMessage();
        }
    }

    private String createPrefixDeclarationStringFromMap(Map<String, String> prefixMap){
        String prefixStr = prefixMap.entrySet().stream()
                .map(e -> "PREFIX " + e.getKey() + ": <" + e.getValue() + ">")
                .collect(Collectors.joining("\n"));
        return prefixStr;
    }

    private Map<String, String> createExampleQueryPrefixMap(){
        Map<String, String> prefixMapQuery = Map.of(
                "query-prefix1", "http://example.org/query-prefix1/",
                "query-prefix2", "http://example.org/query-prefix2/",
                "query-prefix3", "http://example.org/query-prefix3/"
        );
        return prefixMapQuery;
    }

    private Map<String, String> createExampleModelPrefixMap(){
        Map<String, String> prefixMapModel = Map.of(
                "model-prefix1", "http://example.org/model-prefix1/",
                "model-prefix2", "http://example.org/model-prefix2/",
                "model-prefix3", "http://example.org/model-prefix3/"
        );
        return prefixMapModel;
    }
}