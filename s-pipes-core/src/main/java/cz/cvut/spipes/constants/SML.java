package cz.cvut.spipes.constants;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class SML {

    /**
     * The namespace of the vocabulary as a string
     */
    private static final String uri = "http://topbraid.org/sparqlmotionlib#";

    protected static final org.apache.jena.rdf.model.Resource resource(String local )
    { return ResourceFactory.createResource( uri + local ); }

    protected static final Property property(String local )
    { return ResourceFactory.createProperty( uri, local ); }

    public static final Property replace = property( "replace");
    public static final Property constructQuery = property("constructQuery");
    public static final Property value = property("value");
    public static final Property selectQuery = property("selectQuery");
    public static final Property sourceFilePath = property("sourceFilePath");
    public static final Property url = property("url");
    public static final Property targetFilePath = property("targetFilePath");
    public static final Property baseURI = property("baseURI");
    public static final Property serialization = property("serialization");
    public static final Property ignoreImports = property("ignoreImports");

    public static final Resource ApplyConstruct = resource("ApplyConstruct");
    public static final Resource ExportToRDFFile = resource("ExportToRDFFile");
    public static final Resource ImportRDFFromWorkspace = resource("ImportRDFFromWorkspace");
    public static final Resource ImportFileFromURL = resource("ImportFileFromURL");
    public static final Resource BindWithConstant = resource("BindWithConstant");
    public static final Resource BindBySelect = resource("BindBySelect");
    public static final Resource Merge = resource("Merge");
    public static final Resource ReturnRDF = resource("ReturnRDF");

    public static final Resource JSONLD = resource("JSONLD");

    /**
     returns the URI for this schema
     @return the URI for this schema
     */
    public static String getURI() {
        return uri;
    }
}
