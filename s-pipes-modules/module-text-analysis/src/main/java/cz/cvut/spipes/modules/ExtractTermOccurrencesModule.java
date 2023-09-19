package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import cz.cvut.spipes.modules.constants.Termit;
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

@SPipesModule(label = "extract term occurrences", comment =
        "Module extracts term occurrences from annotated literals of input RDF.\n" +
        "<p>\n" +
        "Annotated literals are RDF string literals that are annotated by RDFa\n" +
        "using TermIt terminology to mark occurrences of terms within the text.\n" +
        "</p>\n" +
        "Example of usage:\n" +
        "<p>\n" +
        "Input:\n" +
        "<pre><code>\n" +
        " :x  a csvw:row\n" +
        "     :csat-wo-tc \"4339272\" ;\n" +
        "     :tc-reference \"52-610-00-04\" ;\n" +
        "     :wo-text \"in the &lt;span about=\\\"_:a877-16\\\" property=\\\"ddo:je-výskytem-termu\\\" resource=\\\"http://onto.fel.cvut.cz/ontologies/slovnik/slovnik-komponent-a-zavad---novy/pojem/cvr-ulb\\\" typeof=\\\"ddo:výskyt-termu\\\" score=\\\"0.25\\\"&gt;cockpit&lt;/span&gt;\" ;\n" +
        "</code></pre>\n" +
        "</p>\n" +
        "The expected output:\n" +
        "<pre><code>\n" +
        "     &lt;http://onto.fel.cvut.cz/ontologies/application/termit/pojem/výskyt-termu/instance2fc6112ce9a960c21918569bef6a4151&gt; a       termit-pojem:výskyt-termu ;\n" +
        "        termit-pojem:je-přiřazením-termu &lt;http://onto.fel.cvut.cz/ontologies/slovnik/slovnik-komponent-a-zavad---novy/pojem/cvr-ulb&gt; ;\n" +
        "        termit-pojem:má-cíl\n" +
        "              [ a       termit-pojem:cíl-výskytu ;\n" +
        "                termit-pojem:má-selektor\n" +
        "                        [ a       termit-pojem:selektor-pozici-v-textu ;\n" +
        "                          termit-pojem:má-koncovou-pozici\n" +
        "                                  \"14\"^^&lt;http://www.w3.org/2001/XMLSchema#int&gt; ;\n" +
        "                          termit-pojem:má-startovní-pozici\n" +
        "                                   \"7\"^^&lt;http://www.w3.org/2001/XMLSchema#int&gt;\n" +
        "                        ] ;\n" +
        "                termit-pojem:má-selektor\n" +
        "                        [ a       termit-pojem:selektor-text-quote> ;\n" +
        "                          termit-pojem:má-prefix-text-quote\n" +
        "                                   \"in the \" ;\n" +
        "                          termit-pojem:má-přesný-text-quote\n" +
        "                                   \"cockpit\" ;\n" +
        "                          termit-pojem:má-suffix-text-quote\n" +
        "                                    \"\"\n" +
        "                        ]\n" +
        "              ];\n" +
        "        termit-pojem:má-skóre\n" +
        "                \"0.25\"^^&lt;http://www.w3.org/2001/XMLSchema#float&gt; ;\n" +
        "        termit-pojem:odkazuje-na-anotovaný-text\n" +
        "                \"in the &lt;span about=\\\"_:a877-16\\\" property=\\\"ddo:je-výskytem-termu\\\" resource=\\\"http://onto.fel.cvut.cz/ontologies/slovnik/slovnik-komponent-a-zavad---novy/pojem/cvr-ulb\\\" typeof=\\\"ddo:výskyt-termu\\\" score=\\\"0.25\\\"&gt;cockpit&lt;/span&gt;\" ;\n" +
        "        termit-pojem:odkazuje-na-anotaci\n" +
        "                \"&lt;span about=\\\"_:a877-16\\\" property=\\\"ddo:je-výskytem-termu\\\" resource=\\\"http://onto.fel.cvut.cz/ontologies/slovnik/slovnik-komponent-a-zavad---novy/pojem/cvr-ulb\\\" typeof=\\\"ddo:výskyt-termu\\\" score=\\\"0.25\\\"&gt;cockpit&lt;/span&gt;\" .\n" +
        ".\n" +
        "</code></pre>"
)
public class ExtractTermOccurrencesModule extends AnnotatedAbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(ExtractTermOccurrencesModule.class);

    private static final String TYPE_URI = KBSS_MODULE.uri + "extract-term-occurrences";
    private static final String TYPE_PREFIX = TYPE_URI + "/";

    @Parameter(urlPrefix = SML.uri, name = "replace", comment = "Indicates whether the existing RDF should be overwritten. Default false.") // TODO - revise comment
    private boolean isReplace;

    Extraction extraction = new Extraction();

    @Override
    protected ExecutionContext executeSelf() {
        Model inputRDF = this.getExecutionContext().getDefaultModel();
        Model outputModel = ModelFactory.createDefaultModel();

        Map<String, List<Element>> annotatedElements = new HashMap<>();

        extraction.addPrefix("ddo", Termit.uri);

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
        return this.createOutputContext(isReplace,  outputModel);
    }

    private void createTermOccurrenceResources(Model outputModel, Element e) {
        assert e.parentNode() != null;
        String hash = DigestUtils.md5Hex(StringEscapeUtils.unescapeJava(e.toString()));
        Resource termOccurrence = outputModel.createResource(Termit.VYSKYT_TERMU + "/instance" + hash);
        Resource occurrenceTarget = outputModel.createResource();
        Resource positionSelector = outputModel.createResource();
        Resource textSelector = outputModel.createResource();

        termOccurrence.addProperty(RDF.type, Termit.VYSKYT_TERMU_RESOURCE);
        termOccurrence.addProperty(Termit.JE_PRIRAZENIM_TERMU, outputModel.createResource(fullIri(e.attr(Termit.RDFa.RESOURCE))));
        termOccurrence.addProperty(Termit.MA_CIL, occurrenceTarget);
        termOccurrence.addLiteral(Termit.ODKAZUJE_NA_ANOTOVANY_TEXT, StringEscapeUtils.unescapeJava(((Element) e.parentNode()).html()));
        termOccurrence.addLiteral(Termit.ODKAZUJE_NA_ANOTACI, StringEscapeUtils.unescapeJava(e.toString()));
        if(e.hasAttr(Termit.SCORE)){
            termOccurrence.addLiteral(Termit.MA_SKORE, outputModel.createTypedLiteral(Float.valueOf(e.attr(Termit.SCORE))));
        }

        occurrenceTarget.addProperty(RDF.type, Termit.CIL_VYSKYTU);
        occurrenceTarget.addProperty(Termit.MA_SELEKTOR, textSelector);
        occurrenceTarget.addProperty(Termit.MA_SELEKTOR, positionSelector);

        positionSelector.addProperty(RDF.type, Termit.SELEKTOR_POZICI_V_TEXTU);
        textSelector.addProperty(RDF.type, Termit.SELEKTOR_TEXT_QUOTE);

        String parentTag = ((Element) e.parentNode()).text();
        String textQuote = ((TextNode) e.childNodes().get(0)).text();
        String prefix = parentTag.substring(0, parentTag.indexOf(textQuote));
        String suffix = parentTag.substring(parentTag.indexOf(textQuote) + textQuote.length());

        positionSelector.addLiteral(Termit.MA_STARTOVNI_POZICI, Integer.valueOf(parentTag.indexOf(e.text())));
        positionSelector.addLiteral(Termit.MA_KONCOVOU_POZICI, Integer.valueOf(parentTag.indexOf(e.text()) + e.text().length()));
        textSelector.addLiteral(Termit.MA_PRESNY_TEXT_QUOTE, textQuote);
        textSelector.addLiteral(Termit.MA_PREFIX_TEXT_QUOTE, prefix);
        textSelector.addLiteral(Termit.MA_SUFFIX_TEXT_QUOTE, suffix);
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
