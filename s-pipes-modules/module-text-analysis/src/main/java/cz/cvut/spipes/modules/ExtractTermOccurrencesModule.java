package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.modules.constants.CSVW;
import cz.cvut.spipes.modules.constants.Constants;
import cz.cvut.spipes.modules.textAnalysis.Extraction;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.vocabulary.RDF;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Module for extracting term occurrences from the input
 * <p>
 * The module is responsible for extracting term occurrences from input RDF data
 * and then create corresponding properties in RDF.
 * </p>
 *
 * Example of usage:
 * <p>
 * Input:
 * <pre><code>
 *  :x  a csvw:row
 *      :csat-wo-tc "4339272" ;
 *      :tc-reference "52-610-00-04" ;
 *      :wo-text "<span about="_:a970-5" property="ddo:je-výskytem-termu" resource="http://example.com/term/missing-part" typeof="ddo:výskyt-termu" score="0.5">finding</span>" ;
 * </code></pre>
 * </p>
 * The expected output:
 * <pre><code>
 *  _:a970-5 a ddo:výskyt-termu ;
 *      ddo:je-výskytem-termu "http://example.com/term/missing-part";
 *      :references-annotation "<span about="_:a970-5" property="ddo:je-výskytem-termu" resource="http://example.com/term/missing-part" typeof="ddo:výskyt-termu" score="0.5">finding</span>" ;
 *      :references-text "finding"
 *      termit:má-startovní-pozici "0"^^integer ;
 *      termit:má-koncovou-pozici "7"^^integer ;
 *      termit:má-skóre "0.5"^^integer ;
 * .
 * </code></pre>
 */
public class ExtractTermOccurrencesModule extends AnnotatedAbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(ExtractTermOccurrencesModule.class);

    private static final String TYPE_URI = KBSS_MODULE.uri + "extract-term-occurrences";
    private static final String TYPE_PREFIX = TYPE_URI + "/";

    /** Indicates whether the existing RDF should be overwritten. */
    @Parameter(urlPrefix = SML.uri, name = "replace")
    private boolean isReplace;

    /** The parameter representing the data prefix */
    @Parameter(urlPrefix = TYPE_PREFIX, name = "data-prefix")
    private String dataPrefix;

    Extraction extraction = new Extraction();

    @Override
    protected ExecutionContext executeSelf() {
        Model inputRDF = this.getExecutionContext().getDefaultModel();

        ResIterator rows = inputRDF.listResourcesWithProperty(RDF.type, CSVW.Row);
        Map<String, List<Element>> annotatedElements = new HashMap<>();

        extraction.addPrefix("ddo", Constants.termitUri);

        rows
            .filterDrop(Resource::isAnon)
            .forEach(row -> row.listProperties().forEach(statement -> {
                String text = statement.getObject().toString();
                Document doc = Jsoup.parse(StringEscapeUtils.unescapeJava(text));
                annotatedElements.putAll(extraction.getTermOccurrences(doc.root()));
            }));


        annotatedElements.forEach((key, el) -> {
            Element e = el.get(0);
            Resource res = inputRDF.createResource(key);
            res.addProperty(RDF.type, ResourceFactory.createResource(Constants.VYSKYT_TERMU));

            if(e.hasAttr(Constants.SCORE)){
                res.addLiteral(
                        ResourceFactory.createProperty(Constants.MA_SKORE),
                        inputRDF.createTypedLiteral(Float.valueOf(e.attr(Constants.SCORE)))
                );
            }

            assert e.parentNode() != null;
            String parentTag = ((Element) e.parentNode()).text();

            addLiteral(res, createProperty(Constants.RDFa.RESOURCE), fullIri(e.attr(Constants.RDFa.RESOURCE)));
            addLiteral(res, createProperty(Constants.WHOLE_TEXT), StringEscapeUtils.unescapeJava(((Element) e.parentNode()).html()));
            addLiteral(res, createProperty(Constants.REFERENCES_ANNOTATION), StringEscapeUtils.unescapeJava(e.toString()));
            addLiteral(res, createProperty(Constants.REFERENCES_TEXT), parentTag);
            addLiteral(res, ResourceFactory.createProperty(Constants.JE_VYSKYT_TERMU), e.text());
            addLiteral(res, ResourceFactory.createProperty(Constants.MA_STARTOVNI_POZICI), parentTag.indexOf(e.text()));
            addLiteral(res, ResourceFactory.createProperty(Constants.MA_KONCOVOU_POZICI), parentTag.indexOf(e.text()) + e.text().length());
        });
        return ExecutionContextFactory.createContext(inputRDF);
    }

    private void addLiteral(Resource resource, Property property, Object value){
        resource.addLiteral(property, value);
    }

    private Property createProperty(String uriRef){
        return ResourceFactory.createProperty(getDataPrefix() + uriRef);
    }

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

    private static Property getSpecificParameter(String localPropertyName) {
        return ResourceFactory.createProperty(TYPE_PREFIX + localPropertyName);
    }

    public boolean isReplace() {
        return isReplace;
    }

    public void setReplace(boolean replace) {
        isReplace = replace;
    }

    public String getDataPrefix() {
        return dataPrefix;
    }

    public void setDataPrefix(String dataPrefix) {
        this.dataPrefix = dataPrefix;
    }

    private String fullIri(String possiblyPrefixed) {
        possiblyPrefixed = possiblyPrefixed.trim();
        final int colonIndex = possiblyPrefixed.indexOf(':');
        if (colonIndex == -1) {
            return possiblyPrefixed;
        }
        final String prefix = possiblyPrefixed.substring(0, colonIndex);
        if (!extraction.getPrefixes().containsKey(prefix)) {
            return possiblyPrefixed;
        }
        final String localName = possiblyPrefixed.substring(colonIndex + 1);
        return extraction.getPrefixes().get(prefix) + localName;
    }
}
