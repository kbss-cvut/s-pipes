package cz.cvut.spipes.constants;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class CSVW {

    /**
     * The namespace of the vocabulary as a string
     */
    public static final String uri = "http://www.w3.org/ns/csvw#";
    public static final String extendedUri = "https://onto.fel.cvut.cz/ontologies/csvw-extension/";

    protected static Resource resource(String local )
    { return ResourceFactory.createResource( uri + local ); }

    protected static Property property(String local )
    { return ResourceFactory.createProperty( uri, local ); }

    public static Property extendedProperty(String local )
    { return ResourceFactory.createProperty( uri, local ); }

    public static final Property table = property( "table");
    public static final Property url = property( "url");
    public static final Property row = property( "row");
    public static final Property propertyUrl = property("propertyUrl");
    public static final Property name = property("name");
    public static final Property title = property("title");
    public static final Property column = property("column");
    public static final Property columns = property("columns");


    public static final Resource TableSchema = resource("TableSchema");

    public static final String uriTemplate = uri + "uriTemplate";
    public static final String TableSchemaUri = uri + "TableSchema";
    public static final String aboutUrlUri = uri + "aboutUrl";
    public static final String columnsUri = uri + "columns";
    public static final String propertyUrlUri = uri + "propertyUrl";
    public static final String valueUrlUri = uri + "valueUrl";
    public static final String ColumnUri = uri + "Column";
    public static final String nameUri = uri + "name";
    public static final String titleUri = uri + "title";
    public static final String requiredUri = uri + "required";
    public static final String suppressOutputUri = uri + "suppressOutput";
    public static final String tableGroupUri = uri + "TableGroup";
    public static final String tableUri = uri + "table";
    public static final String rowUri = uri + "row";
    public static final String RowUri = uri + "Row";
    public static final String URL = uri + "url";
    public static final String rowNumUri = uri + "rownum";
    public static final String describesUri = uri + "describes";
    public static final String TableUri = uri + "Table";
    public static final String tableSchemaUri = uri + "tableSchema";
    public static final String extendedPropertyUri = extendedUri + "property";
    /**
     returns the URI for this schema
     @return the URI for this schema
     */
    public static String getURI() {
        return uri;
    }
}
