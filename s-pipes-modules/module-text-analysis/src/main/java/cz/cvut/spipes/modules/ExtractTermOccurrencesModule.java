package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.modules.constants.Constants;
import cz.cvut.spipes.modules.textAnalysis.Extraction;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.jena.datatypes.xsd.impl.XSDBaseStringType;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
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
 *      :wo-text "in the &lt;span about=\"_:a877-16\" property=\"ddo:je-výskytem-termu\" resource=\"http://onto.fel.cvut.cz/ontologies/slovnik/slovnik-komponent-a-zavad---novy/pojem/cvr-ulb\" typeof=\"ddo:výskyt-termu\" score=\"0.25\"&gt;cockpit&lt;/span&gt;" ;
 * </code></pre>
 * </p>
 * The expected output:
 * <pre><code>
 *      &lt;http://onto.fel.cvut.cz/ontologies/application/termit/pojem/výskyt-termu/instance2fc6112ce9a960c21918569bef6a4151&gt; a       termit-pojem:výskyt-termu ;
 *         termit-pojem:je-přiřazením-termu &lt;http://onto.fel.cvut.cz/ontologies/slovnik/slovnik-komponent-a-zavad---novy/pojem/cvr-ulb&gt; ;
 *         termit-pojem:má-cíl
 *               [ a       termit-pojem:cíl-výskytu ;
 *                 termit-pojem:má-selektor
 *                         [ a       termit-pojem:selektor-pozici-v-textu ;
 *                           termit-pojem:má-koncovou-pozici
 *                                   "14"^^&lt;http://www.w3.org/2001/XMLSchema#int&gt; ;
 *                           termit-pojem:má-startovní-pozici
 *                                    "7"^^&lt;http://www.w3.org/2001/XMLSchema#int&gt;
 *                         ] ;
 *                 termit-pojem:má-selektor
 *                         [ a       termit-pojem:selektor-text-quote> ;
 *                           termit-pojem:má-prefix-text-quote
 *                                    "in the " ;
 *                           termit-pojem:má-přesný-text-quote
 *                                    "cockpit" ;
 *                           termit-pojem:má-suffix-text-quote
 *                                     ""
 *                         ]
 *               ];
 *         termit-pojem:má-skóre
 *                 "0.25"^^&lt;http://www.w3.org/2001/XMLSchema#float&gt; ;
 *         termit-pojem:odkazuje-na-anotovaný-text
 *                 "in the &lt;span about=\"_:a877-16\" property=\"ddo:je-výskytem-termu\" resource=\"http://onto.fel.cvut.cz/ontologies/slovnik/slovnik-komponent-a-zavad---novy/pojem/cvr-ulb\" typeof=\"ddo:výskyt-termu\" score=\"0.25\"&gt;cockpit&lt;/span&gt;" ;
 *         termit-pojem:odkazuje-na-anotaci
 *                 "&lt;span about=\"_:a877-16\" property=\"ddo:je-výskytem-termu\" resource=\"http://onto.fel.cvut.cz/ontologies/slovnik/slovnik-komponent-a-zavad---novy/pojem/cvr-ulb\" typeof=\"ddo:výskyt-termu\" score=\"0.25\"&gt;cockpit&lt;/span&gt;" .
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
        Model outputModel = ModelFactory.createDefaultModel();

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

        annotatedElements.forEach((key, el) -> createTermOccurrenceResources(outputModel, el.get(0)));
        return this.createOutputContext(isReplace, inputRDF, outputModel);
    }

    private void createTermOccurrenceResources(Model outputModel, Element e) {
        assert e.parentNode() != null;
        String hash = DigestUtils.md5Hex(StringEscapeUtils.unescapeJava(e.toString()));
        Resource termOccurrence = outputModel.createResource(Constants.VYSKYT_TERMU + "/instance" + hash);
        Resource occurrenceTarget = outputModel.createResource();
        Resource positionSelector = outputModel.createResource();
        Resource textSelector = outputModel.createResource();

        termOccurrence.addProperty(RDF.type, Constants.VYSKYT_TERMU_RESOURCE);
        termOccurrence.addProperty(Constants.JE_PRIRAZENIM_TERMU, outputModel.createResource(fullIri(e.attr(Constants.RDFa.RESOURCE))));
        termOccurrence.addProperty(Constants.MA_CIL, occurrenceTarget);
        termOccurrence.addLiteral(Constants.ODKAZUJE_NA_ANOTOVANY_TEXT, StringEscapeUtils.unescapeJava(((Element) e.parentNode()).html()));
        termOccurrence.addLiteral(Constants.ODKAZUJE_NA_ANOTACI, StringEscapeUtils.unescapeJava(e.toString()));
        if(e.hasAttr(Constants.SCORE)){
            termOccurrence.addLiteral(Constants.MA_SKORE, outputModel.createTypedLiteral(Float.valueOf(e.attr(Constants.SCORE))));
        }

        occurrenceTarget.addProperty(RDF.type, Constants.CIL_VYSKYTU);
        occurrenceTarget.addProperty(Constants.MA_SELEKTOR, textSelector);
        occurrenceTarget.addProperty(Constants.MA_SELEKTOR, positionSelector);

        positionSelector.addProperty(RDF.type, Constants.SELEKTOR_POZICI_V_TEXTU);
        textSelector.addProperty(RDF.type, Constants.SELEKTOR_TEXT_QUOTE);

        String parentTag = ((Element) e.parentNode()).text();
        String textQuote = ((TextNode) e.childNodes().get(0)).text();
        String prefix = parentTag.substring(0, parentTag.indexOf(textQuote));
        String suffix = parentTag.substring(parentTag.indexOf(textQuote) + textQuote.length());

        positionSelector.addLiteral(Constants.MA_STARTOVNI_POZICI, Integer.valueOf(parentTag.indexOf(e.text())));
        positionSelector.addLiteral(Constants.MA_KONCOVOU_POZICI, Integer.valueOf(parentTag.indexOf(e.text()) + e.text().length()));
        textSelector.addLiteral(Constants.MA_PRESNY_TEXT_QUOTE, textQuote);
        textSelector.addLiteral(Constants.MA_PREFIX_TEXT_QUOTE, prefix);
        textSelector.addLiteral(Constants.MA_SUFFIX_TEXT_QUOTE, suffix);
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
