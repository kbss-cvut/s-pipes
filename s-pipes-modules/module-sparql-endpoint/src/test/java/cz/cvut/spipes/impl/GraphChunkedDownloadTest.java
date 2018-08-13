package cz.cvut.spipes.impl;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class GraphChunkedDownloadTest {

    protected Query testQueryCanParse(String query){
        // test query parsing - exception thrown - test fails, success otherwise
        return QueryFactory.create(query);
    }

    protected GraphChunkedDownload createInstance(Dataset ds, Model downloadedGraph, String datasetURI, String graphURI, int pageSize){
        return new GraphChunkedDownload(datasetURI, graphURI, pageSize) {
            @Override
            protected void processPartialModel(Model partialModel) {
                downloadedGraph.add(partialModel);
            }

            @Override
            protected Model executeQuery(String query) {
                // test query parsing - exception thrown - test fails, success otherwise
                Query q = testQueryCanParse(query);
                // to test whether the query uses the correct graph
                return QueryExecutionFactory.create(q, ds).execConstruct();
            }

            @Override
            protected String getInnerSelect() {
                String qs = super.getInnerSelect();
                testQueryCanParse(qs);
                return qs;
            }

            @Override
            protected String prepareQuery(long offset) {
                assertEquals(0, offset%pageSize);
                String qs = super.prepareQuery(offset);
                testQueryCanParse(qs);
                return qs;
            }
        };
    }


    @Test
    public void testExecute() {
        // mock database
        String datasetURI = "http://example.org/dataset";
        String graphURI = "http://example.org/dataset/graph1";
        int pageSize = 10;
        Dataset ds = DatasetFactory.createGeneral();

        int defaultGraphSize = 20;
        int namedGraphSize = 30;

        Model defaultGraph = ModelFactory.createDefaultModel();
        IntStream.range(0,defaultGraphSize).mapToObj(i -> i).forEach(i -> defaultGraph.add(ResourceFactory.createResource(), RDF.type, RDFS.Class));
        Model namedGraph = ModelFactory.createDefaultModel();
        IntStream.range(0,namedGraphSize).mapToObj(i -> i).forEach(i -> namedGraph.add(ResourceFactory.createResource(), RDF.type, RDFS.Class));

        ds.setDefaultModel(defaultGraph);
        ds.addNamedModel(graphURI, namedGraph);

        // test named graph
        Model downloadedGraph = ModelFactory.createDefaultModel();
        GraphChunkedDownload downloader = createInstance(ds, downloadedGraph, datasetURI, graphURI, pageSize);
        downloader.execute();
        assertEquals(downloadedGraph.size(), namedGraphSize);

        // test for default graph
        downloadedGraph = ModelFactory.createDefaultModel();
        downloader = createInstance(ds, downloadedGraph, datasetURI, null, pageSize);
        downloader.execute();
        assertEquals(downloadedGraph.size(), defaultGraphSize);

    }

}