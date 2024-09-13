package cz.cvut.spipes.constants;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;

public class SML {

    /**
     * The namespace of the vocabulary as a string
     */
    public static final String uri = "http://topbraid.org/sparqlmotionlib#";

    public static final String replace = uri + "replace";
    public static final String constructQuery = uri + "constructQuery";
    public static final String value = uri + "value";
    public static final String selectQuery = uri + "selectQuery";
    public static final String updateQuery = uri + "updateQuery";
    public static final String sourceFilePath = uri + "sourceFilePath";
    public static final String url = uri + "url";
    public static final String targetFilePath = uri + "targetFilePath";
    public static final String baseURI = uri + "baseURI";
    public static final String serialization = uri + "serialization";
    public static final String ignoreImports = uri + "ignoreImports";

    public static final String ApplyConstruct = uri + "ApplyConstruct";
    public static final String ExportToRDFFile = uri + "ExportToRDFFile";
    public static final String ImportRDFFromWorkspace = uri + "ImportRDFFromWorkspace";
    public static final String ImportFileFromURL = uri + "ImportFileFromURL";
    public static final String BindWithConstant = uri + "BindWithConstant";
    public static final String BindBySelect = uri + "BindBySelect";
    public static final String Merge = uri + "Merge";
    public static final String ReturnRDF = uri + "ReturnRDF";
    public static final String JSONLD = uri + "JSONLD";

    public static class JENA {
        public static final Property replace = createProperty(SML.replace);
        public static final Property constructQuery = createProperty(SML.constructQuery);
        public static final Property value = createProperty(SML.value);
        public static final Property selectQuery = createProperty(SML.selectQuery);
        public static final Property updateQuery = createProperty(SML.updateQuery);
        public static final Property sourceFilePath = createProperty(SML.sourceFilePath);
        public static final Property url = createProperty(SML.url);
        public static final Property targetFilePath = createProperty(SML.targetFilePath);
        public static final Property baseURI = createProperty(SML.baseURI);
        public static final Property serialization = createProperty(SML.serialization);
        public static final Property ignoreImports = createProperty(SML.ignoreImports);

        public static final Resource ApplyConstruct = createResource(SML.ApplyConstruct);
        public static final Resource ExportToRDFFile = createResource(SML.ExportToRDFFile);
        public static final Resource ImportRDFFromWorkspace = createResource(SML.ImportRDFFromWorkspace);
        public static final Resource ImportFileFromURL = createResource(SML.ImportFileFromURL);
        public static final Resource BindWithConstant = createResource(SML.BindWithConstant);
        public static final Resource BindBySelect = createResource(SML.BindBySelect);
        public static final Resource Merge = createResource(SML.Merge);
        public static final Resource ReturnRDF = createResource(SML.ReturnRDF);
        public static final Resource JSONLD = createResource(SML.JSONLD);
    }
}
