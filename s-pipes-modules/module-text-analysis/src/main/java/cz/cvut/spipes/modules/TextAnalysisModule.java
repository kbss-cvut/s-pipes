package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import cz.cvut.spipes.modules.constants.Termit;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.jena.datatypes.xsd.impl.XSDBaseStringType;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.model.Select;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;
import static org.apache.commons.lang.StringEscapeUtils.unescapeHtml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Module for text analysis.
 * <p>
 * This class provides a module for text analysis.
 * It uses an external web service to analyze text data and retrieve annotated text.
 * It analyse the text using a SKOS vocabulary that is stored in RDF4J repository.
 * </p>
 */
@SPipesModule(label = "Text analysis module", comment = "test comment")
public class TextAnalysisModule extends AnnotatedAbstractModule{

    private static final Logger LOG = LoggerFactory.getLogger(TextAnalysisModule.class);
    private static final String TYPE_URI = KBSS_MODULE.uri + "text-analysis";
    private static final String TYPE_PREFIX = TYPE_URI + "/";

    /** The URL of the text analysis service to be used. */
    @Parameter(urlPrefix = TYPE_PREFIX, name = "service-url")
    private String serviceUrl;

    /** The IRI of the vocabulary to be used for entity recognition. */
    @Parameter(urlPrefix = TYPE_PREFIX, name = "vocabulary-iri")
    private String vocabularyIri;

    /** The IRI of the repository where the vocabulary is stored. */
    @Parameter(urlPrefix = TYPE_PREFIX, name = "vocabulary-repository")
    private String vocabularyRepository;

    /** The language of the text to be analyzed. */
    @Parameter(urlPrefix = TYPE_PREFIX, name = "language")
    private String language;

    //sml:replace
    @Parameter(urlPrefix = SML.uri, name = "replace")
    private boolean isReplace = false;

    /** The number of literals to be processed per request to the web service. */
    @Parameter(urlPrefix = TYPE_PREFIX, name = "literals-per-request")
    private Integer literalsPerRequest;

    /** The SPARQL query to be used for selecting literals from the repository.
     * <p>
     * Example:
     * <pre>{@code
     * SELECT ?literal
     * WHERE {
     *    ?s ?p ?literal .
     *    FILTER(isLiteral(?literal) && datatype(?literal) = xsd:string)
     * }
     * }</pre>
     */
    private Select selectQuery;

    @Override
    ExecutionContext executeSelf() {
        Model inputModel = this.getExecutionContext().getDefaultModel();
        Model outputModel = ModelFactory.createDefaultModel();

        if (selectQuery == null) {
            LOG.warn("Select query is empty therefore returning input model.");
            return executionContext;
        }

        Query query = ARQFactory.get().createQuery(selectQuery);
        try (QueryExecution queryExecution = QueryExecutionFactory.create(query, inputModel)) {
            ResultSet resultSet = queryExecution.execSelect();
            List<RDFNode> listOfObjects = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            int counter = 0;
            int totalCounter = 0;

            while (resultSet.hasNext()) {
                QuerySolution solution = resultSet.nextSolution();
                Iterator<String> variableBindings = solution.varNames();
                while (variableBindings.hasNext()){
                    RDFNode object = solution.get(variableBindings.next());

                    if (!object.isLiteral() || !(object.asLiteral().getDatatype() instanceof XSDBaseStringType)) {
                        LOG.warn("Object {} is not a literal. Skipping.", object);
                        continue;
                    }

                    Literal literal = object.asLiteral();
                    String textElement = escapeHtml(literal.getString());
                    if (counter >= literalsPerRequest) {
                        LOG.debug("Annotating {} literals. Progress {}%.", literalsPerRequest, totalCounter * 100L / inputModel.size());
                        String annotatedText = annotateObjectLiteral(sb.toString());
                        String[] elements = splitAnnotatedText(annotatedText);

                        for (int i = 0; i < listOfObjects.size(); i++) {
                            String annotatedTerm = unescapeHtml(elements[i]);
                            createAnnotatedResource(outputModel, textElement, annotatedTerm);
                        }
                        listOfObjects.clear();
                        sb = new StringBuilder();
                        counter = 0;
                    }

                    listOfObjects.add(object);
                    sb.append(textElement);
                    sb.append("<br>");
                    counter++;
                    totalCounter++;
                }
            }

            if (counter > 0) {
                LOG.debug("Annotating {} literals. Progress {}%.", literalsPerRequest, totalCounter * 100L / inputModel.size());
                String annotatedText = annotateObjectLiteral(sb.toString());
                String[] elements = splitAnnotatedText(annotatedText);

                for (int i = 0; i < listOfObjects.size(); i++) {
                    RDFNode obj = listOfObjects.get(i);
                    String textElement = obj.asLiteral().getString();
                    String annotatedTerm = elements[i];
                    createAnnotatedResource(outputModel, textElement, annotatedTerm);
                }
            }
        }
        return createOutputContext(isReplace, inputModel, outputModel);
    }

    private void createAnnotatedResource(Model outputModel, String originalText, String annotatedText) {
        Resource annotatedResource = outputModel.createResource();

        annotatedResource.addProperty(RDF.type, Termit.ANNOTATION);
        annotatedResource.addProperty(Termit.ORIGINAL_TEXT, originalText);
        annotatedResource.addProperty(Termit.ANNOTATED_TEXT, annotatedText);
    }

    private String[] splitAnnotatedText(String annotatedText) {
        return Jsoup.parse(annotatedText).body().html().split(" <br>");
    }

    @NotNull
    private String annotateObjectLiteral(String objectValue) {
        HttpPost request = new HttpPost(serviceUrl);
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("content", objectValue);
        jsonObject.put("vocabularyRepository", vocabularyRepository);
        jsonObject.put("language", language);
        jsonObject.append("vocabularyContexts", vocabularyIri);

        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(jsonObject.toString(), "UTF-8"));

        String annotatedText;
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpResponse response = httpClient.execute(request);

            Document doc = Jsoup.parse(EntityUtils.toString(response.getEntity()));
            Element body = doc.body();
            annotatedText = body.html();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return annotatedText;
    }

    @Override
    public void loadConfiguration() {
        super.loadConfiguration();
        selectQuery = getPropertyValue(SML.selectQuery).asResource().as(Select.class);
    }

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }
}
