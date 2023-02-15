package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.modules.constants.Constants;
import cz.cvut.spipes.modules.textAnalysis.Extraction;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.jena.datatypes.xsd.impl.XSDBaseStringType;
import org.apache.jena.rdf.model.*;
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
 * Module extracts term occurrences from annotated literals of input RDF.
 * <p>
 * Annotated literals are RDF string literals that are annotated by RDFa
 * using TermIt terminology to mark occurrences of terms within the text.
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
 *  _:a970-5 a termit:výskyt-termu ;
 *      termit:je-výskytem-termu <http://example.com/term/missing-part>;
 *      :references-annotation "<span about="_:a970-5" property="ddo:je-výskytem-termu" resource="http://example.com/term/missing-part" typeof="ddo:výskyt-termu" score="0.5">finding</span>" ;
 *      termit:má-přesný-text-quote "finding"
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

    Extraction extraction = new Extraction();

    @Override
    protected ExecutionContext executeSelf() {
        Model inputRDF = this.getExecutionContext().getDefaultModel();

        Map<String, List<Element>> annotatedElements = new HashMap<>();

        extraction.addPrefix("ddo", Constants.termitUri);

        inputRDF.listObjects().
                filterKeep(o -> o.isLiteral() && o.asLiteral().getDatatype() instanceof XSDBaseStringType)
                .forEach(
                        str -> {
                            String text = str.asLiteral().getString();
                            Document doc = Jsoup.parse(StringEscapeUtils.unescapeJava(text));
                            annotatedElements.putAll(extraction.getTermOccurrences(doc.root()));
                        }
                );

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

            res.addProperty(ResourceFactory.createProperty(Constants.JE_VYSKYT_TERMU), ResourceFactory.createResource(fullIri(e.attr(Constants.RDFa.RESOURCE))));
            addLiteral(res, ResourceFactory.createProperty(Constants.WHOLE_TEXT), StringEscapeUtils.unescapeJava(((Element) e.parentNode()).html()));
            addLiteral(res, ResourceFactory.createProperty(Constants.REFERENCES_ANNOTATION), StringEscapeUtils.unescapeJava(e.toString()));
            addLiteral(res, ResourceFactory.createProperty(Constants.MA_PRESNY_TEXT_QUOTE), parentTag);
            addLiteral(res, ResourceFactory.createProperty(Constants.MA_STARTOVNI_POZICI), parentTag.indexOf(e.text()));
            addLiteral(res, ResourceFactory.createProperty(Constants.MA_KONCOVOU_POZICI), parentTag.indexOf(e.text()) + e.text().length());
        });
        return ExecutionContextFactory.createContext(inputRDF);
    }

    private void addLiteral(Resource resource, Property property, Object value){
        resource.addLiteral(property, value);
    }

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

    public boolean isReplace() {
        return isReplace;
    }

    public void setReplace(boolean replace) {
        isReplace = replace;
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
