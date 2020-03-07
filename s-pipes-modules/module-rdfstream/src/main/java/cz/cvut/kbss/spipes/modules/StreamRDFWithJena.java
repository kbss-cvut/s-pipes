package cz.cvut.kbss.spipes.modules;

import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.atlas.lib.SinkNull;
import org.apache.jena.atlas.lib.SinkSplit;
import org.apache.jena.graph.Triple;
import org.apache.jena.mem.GraphMem;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.impl.ModelCom;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 *
 * @author Bogdan Kostov <bogdan.kostov@fel.cvut.cz>
 */
public class StreamRDFWithJena {

    public static void main(String[] args) throws Exception {
        String fileName = "d:\\downloads\\persondata_en.ttl\\persondata_en.ttl";
//        String fileName = "d:\\downloads\\persondata_en.ttl\\persons_fragment.ttl";
//        streamTriplesUsingJenasStreamApiOneSink("d:\\downloads\\persondata_en.ttl\\persondata_en.ttl", Lang.TURTLE);
        streamTriplesUsingJenasStreamApiTwoSinks(fileName, Lang.TURTLE);
//        streamTriplesUsingGraphMem(fileName, Lang.TURTLE);
    }
    
    /**
     * Count resources (uris) in the stream of triples using the StreamRDF
     *
     * @param inputFile
     * @param lang
     * @throws Exception
     */
    public static void streamTriplesUsingJenasStreamApiOneSink(String inputFile, Lang lang) throws Exception {
        Sink<Triple> countResources = new CountURISink();
        // create a streamRDF with the sink
        StreamRDF streamRDF = StreamRDFLib.sinkTriples(countResources);

//        StreamOps.
        InputStream is = new FileInputStream(inputFile);
        // parse the InputStream is an transform it to a streamRDF stream of triples/quads (in this case triples)
        RDFDataMgr.parse(streamRDF, is, lang);
    }

    /**
     * Comobine two sinks Count resources (uris) and count distinct namspaces in
     * the stream of triples using the StreamRDF
     *
     * @param inputFile
     * @param lang
     * @throws Exception
     */
    public static void streamTriplesUsingJenasStreamApiTwoSinks(String inputFile, Lang lang) throws Exception {

        Sink<Triple> multipleSinks = new SinkSplit<Triple>(
                new CountURISink(),// sink to count resources
                new CountDistinctNamespacesSink() // sink to count the distinct namespaces
        );
                
        // create a streamRDF with the sink
        StreamRDF streamRDF = StreamRDFLib.sinkTriples(multipleSinks);

//        StreamOps.
        InputStream is = new FileInputStream(inputFile);
        // parse the InputStream is an transform it to a streamRDF stream of triples/quads (in this case triples)
        RDFDataMgr.parse(streamRDF, is, lang);
    }
    
    public static class CountURISink extends SinkNull<Triple> {
        protected long numberOfResources = 0;

        /**
         * This method is called for each parsed triple.
         *
         * @param t
         */
        @Override
        public void send(Triple t) {
            // code to process triple
            Stream.of(t.getSubject(), t.getPredicate(), t.getObject()).
                    filter(n -> n.isURI()).
                    forEach(r -> numberOfResources++);
        }

        /**
         * This method is called after the stream of triples is finished.
         */
        @Override
        public void flush() {
            System.out.println(String.format("the number of resouces in the file is - %d.", numberOfResources));
        }
    };
    
    public static class CountDistinctNamespacesSink extends SinkNull<Triple>{

        protected Pattern nsPattern = Pattern.compile("^([^()]+[#/])[^#/]+$");
        protected Set<String> namespaces = new HashSet<String>();
        
        /**
         * Count the distinct namespaces found used in the uris of the triple stream
         * @param t 
         */
        @Override
        public void send(Triple t) {
            Stream.of(t.getSubject(), t.getPredicate(), t.getObject()).
                    filter(n -> n.isURI()).
                    map(r -> getNamespace(r.getURI().toString())).
                    filter(ns -> ns != null).
                    forEach(ns -> namespaces.add(ns));
        }

        protected String getNamespace(String uri) {
            // this is a naive way of recognizing name spaces.
            Matcher m = nsPattern.matcher(uri);
            if (m.matches()) {
                return m.group(1);
            }
            return null;
        }
        
        
        /**
         * This method is called after the stream of triples is finished.
         */
        @Override
        public void flush() {
            System.out.println(String.format("There are %d distinct namespaces :", namespaces.size()));
            namespaces.forEach(System.out::println);
        }

    };

    protected static long numberOfResources = 0;

    /**
     * Count resources (uris) in the stream of triples by subclassing GraphMem
     * and implementing the GraphMem.add(Triple t) method.
     *
     * @param inputFile
     * @param lang
     * @throws Exception
     * @deprecated use the RDFSTream and Snik api, it is more flexible
     */
    @Deprecated
    public static void streamTriplesUsingGraphMem(String inputFile, Lang lang) throws Exception {
        Model m = new ModelCom(new GraphMem() {
            @Override
            public void add(Triple t) {
                Stream.of(t.getSubject(), t.getPredicate(), t.getObject()).
                        filter(n -> n.isURI()).
                        forEach(r -> numberOfResources++);
            }
        });
        InputStream is = new FileInputStream(inputFile);
        m.read(is, null, lang.getLabel());
        System.out.println(String.format("the number of resouces in the file is - %d.", numberOfResources));
    }
}
