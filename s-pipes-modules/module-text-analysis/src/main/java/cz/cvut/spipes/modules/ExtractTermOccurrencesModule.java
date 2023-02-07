package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.exception.ResourceNotFoundException;
import cz.cvut.spipes.modules.constants.Constants;
import cz.cvut.spipes.modules.textAnalysis.Extraction;
import cz.cvut.spipes.registry.StreamResource;
import cz.cvut.spipes.registry.StreamResourceRegistry;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtractTermOccurrencesModule extends AnnotatedAbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(ExtractTermOccurrencesModule.class);

    private static final String TYPE_URI = KBSS_MODULE.uri + "extract-term-occurrences";
    private static final String TYPE_PREFIX = TYPE_URI + "/";

    @Parameter(urlPrefix = SML.uri, name = "replace")
    private boolean isReplace;

    @Parameter(urlPrefix = TYPE_PREFIX, name = "data-prefix")
    private String dataPrefix;

    @Parameter(urlPrefix = TYPE_PREFIX, name = "source-resource-uri")
    private String streamResourceUri;

    private StreamResource sourceResource;

    Extraction extraction = new Extraction();

    @Override
    protected ExecutionContext executeSelf() {
        Model inputRDF = this.getExecutionContext().getDefaultModel();

        ResIterator rows = inputRDF.listResourcesWithProperty(RDF.type, Constants.CSVW_ROW_URI);
        Map<String, List<Element>> annotatedElements = new HashMap<>();

        extraction.addPrefix("ddo","http://onto.fel.cvut.cz/ontologies/application/termit/pojem/");

        rows.forEach(row -> {
            String text = row.getRequiredProperty(createProperty("WO_text")).getObject().toString();
            Document doc = Jsoup.parse(StringEscapeUtils.unescapeJava(text));
            annotatedElements.putAll(extraction.getTermOccurrences(doc.root()));
        });


        annotatedElements.forEach((key, el) -> {
            Element e = el.get(0);
            Resource res = inputRDF.createResource(key);
            res.addProperty(RDF.type, ResourceFactory.createResource(Constants.VYSKYT_TERMU));

            if(e.hasAttr(Constants.SCORE)){
                res.addLiteral(
                        ResourceFactory.createProperty(getDataPrefix() + Constants.SCORE),
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
            addLiteral(res, ResourceFactory.createProperty(Constants.MA_STARTOVNi_POZICI), parentTag.indexOf(e.text()));
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

    @Override
    public void loadConfiguration() {
        super.loadConfiguration();
        sourceResource = getResourceByUri(streamResourceUri);
    }

    @NotNull
    private StreamResource getResourceByUri(@NotNull String resourceUri) {

        StreamResource res = StreamResourceRegistry.getInstance().getResourceByUrl(resourceUri);

        if (res == null) {
            throw new ResourceNotFoundException("Stream resource " + resourceUri + " not found. ");
        }
        return res;
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

    public StreamResource getSourceResource() {
        return sourceResource;
    }

    public void setSourceResource(StreamResource sourceResource) {
        this.sourceResource = sourceResource;
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
