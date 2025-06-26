package cz.cvut.spipes.spin.vocabulary;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Vocabulary for http://spinrdf.org/spr
 *
 * @author Holger Knublauch
 */
public class SPR {

    public final static String BASE_URI = "http://spinrdf.org/spr";

    public final static String NS = BASE_URI + "#";

    public final static String PREFIX = "spr";


    public final static Resource Table = ResourceFactory.createResource(NS + "Table");

    public final static Resource TableClass = ResourceFactory.createResource(NS + "TableClass");

    public final static Resource cell = ResourceFactory.createResource(NS + "cell");

    public final static Resource cellFunction = ResourceFactory.createResource(NS + "cellFunction");

    public final static Resource colCount = ResourceFactory.createResource(NS + "colCount");

    public final static Resource colCountFunction = ResourceFactory.createResource(NS + "colCountFunction");

    public final static Resource colName = ResourceFactory.createResource(NS + "colName");

    public final static Resource colNameFunction = ResourceFactory.createResource(NS + "colNameFunction");

    public final static Resource colTypeFunction = ResourceFactory.createResource(NS + "colTypeFunction");

    public final static Resource colWidthFunction = ResourceFactory.createResource(NS + "colWidthFunction");

    public final static Resource colType = ResourceFactory.createResource(NS + "colType");

    public final static Resource colWidth = ResourceFactory.createResource(NS + "colWidth");

    public final static Resource contains = ResourceFactory.createResource(NS + "contains");

    public final static Resource hasCell = ResourceFactory.createResource(NS + "hasCell");

    public final static Resource hasCellFunction = ResourceFactory.createResource(NS + "hasCellFunction");

    public final static Resource rowCount = ResourceFactory.createResource(NS + "rowCount");

    public final static Resource rowCountFunction = ResourceFactory.createResource(NS + "rowCountFunction");

    public final static Resource union = ResourceFactory.createResource(NS + "union");
}
