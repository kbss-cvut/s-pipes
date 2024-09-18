package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import cz.cvut.spipes.modules.constants.Termit;
import cz.cvut.spipes.util.QueryUtils;
import lombok.extern.slf4j.Slf4j;
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
import org.topbraid.spin.model.Select;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;
import static org.apache.commons.lang.StringEscapeUtils.unescapeHtml;

@Slf4j
@SPipesModule(label = "Text analysis module", comment =
"Module for text analysis.\n" +
"<p>\n" +
"This class provides a module for text analysis.\n" +
"It uses an external web service to analyze text data and retrieve annotated text.\n" +
"It analyse the text using a SKOS vocabulary that is stored in RDF4J repository.\n" +
"</p>"
)
public class TextAnalysisModule extends AnnotatedAbstractModule{

    private static final String TYPE_URI = KBSS_MODULE.uri + "text-analysis";
    private static final String TYPE_PREFIX = TYPE_URI + "/";

    @Parameter(iri = TYPE_PREFIX + "service-url", comment = "The URL of the text analysis service to be used.")
    private String serviceUrl;

    @Parameter(iri = TYPE_PREFIX + "vocabulary-iri", comment = "The IRI of the vocabulary to be used for entity recognition.")
    private String vocabularyIri;

    @Parameter(iri = TYPE_PREFIX + "vocabulary-repository", comment = "The IRI of the repository where the vocabulary is stored.")
    private String vocabularyRepository;

    @Parameter(iri = TYPE_PREFIX + "language", comment = "The language of the text to be analyzed.")
    private String language;

    @Parameter(iri = SML.replace, comment = "Replace context flag, default is false.")
    private boolean isReplace = false;

    @Parameter(iri = TYPE_PREFIX + "literals-per-request", comment = "The number of literals to be processed per request to the web service.")
    private Integer literalsPerRequest;

    @Parameter(iri = SML.selectQuery,
            comment =
                "The SPARQL query to be used for selecting literals from the repository.\n" +
                "<p>\n" +
                "Example:\n" +
                "<pre>{@code\n" +
                "SELECT ?literal\n" +
                "WHERE {\n" +
                "    ?s ?p ?literal .\n" +
                "    FILTER(isLiteral(?literal) && datatype(?literal) = xsd:string)\n" +
                "}\n" +
                "}</pre>"
            )
    private Select selectQuery;

    @Override
    ExecutionContext executeSelf() {
        Model inputModel = this.getExecutionContext().getDefaultModel();
        Model outputModel = ModelFactory.createDefaultModel();

        if (selectQuery == null) {
            log.warn("Select query is empty therefore returning input model.");
            return executionContext;
        }

        Query query = QueryUtils.createQuery(selectQuery);
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
                        log.warn("Object {} is not a literal. Skipping.", object);
                        continue;
                    }

                    Literal literal = object.asLiteral();
                    String textElement = escapeHtml(literal.getString());
                    if (counter >= literalsPerRequest) {
                        log.debug("Annotating {} literals. Progress {}%.", literalsPerRequest, totalCounter * 100L / inputModel.size());
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
                log.debug("Annotating {} literals. Progress {}%.", literalsPerRequest, totalCounter * 100L / inputModel.size());
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
        return createOutputContext(isReplace, outputModel);
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
    public void loadManualConfiguration() {
        super.loadConfiguration();
    }

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }
}
