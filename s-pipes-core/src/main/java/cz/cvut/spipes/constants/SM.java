package cz.cvut.spipes.constants;

import org.apache.jena.rdf.model.Property;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;

public class SM {

    /**
     * The namespace of the vocabulary as a string
     */
    public static final String uri = "http://topbraid.org/sparqlmotion#";

    public static final String next = uri + "next";
    public static final String outputVariable = uri + "outputVariable";
    public static final String returnModule = uri + "returnModule";

    public static final String Function = uri + "Function";
    public static final String Module = uri + "Module";
    public static final String Modules = uri + "Modules";


    public static class JENA {
        public static final Property next = createProperty(SM.next);
        public static final Property outputVariable = createProperty(SM.outputVariable);
        public static final Property returnModule = createProperty(SM.returnModule);

        public static final org.apache.jena.rdf.model.Resource Function = createResource(SM.Function);
        public static final org.apache.jena.rdf.model.Resource Module = createResource(SM.Module);
        public static final org.apache.jena.rdf.model.Resource Modules = createResource(SM.Modules);
    }
}
