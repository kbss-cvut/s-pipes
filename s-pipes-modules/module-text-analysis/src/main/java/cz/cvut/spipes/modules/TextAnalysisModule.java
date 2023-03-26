package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import cz.cvut.spipes.modules.constants.Termit;
import org.apache.commons.codec.digest.DigestUtils;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SPipesModule(label = "Text analysis module", comment = "test comment")
public class TextAnalysisModule extends AnnotatedAbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(TextAnalysisModule.class);
    private static final String TYPE_URI = KBSS_MODULE.uri + "text-analysis";
    private static final String TYPE_PREFIX = TYPE_URI + "/";

    @Parameter(urlPrefix = TYPE_PREFIX, name = "service-url")
    private String serviceUrl;

    @Parameter(urlPrefix = TYPE_PREFIX, name = "vocabulary-iri")
    private String vocabularyIri;

    @Parameter(urlPrefix = TYPE_PREFIX, name = "vocabulary-repository")
    private String vocabularyRepository;

    @Parameter(urlPrefix = TYPE_PREFIX, name = "language")
    private String language;

    @Parameter(urlPrefix = SML.uri, name = "replace")
    private boolean isReplace = false;

    @Parameter(urlPrefix = TYPE_PREFIX, name = "literals-per-request")
    private Integer literalsPerRequest;

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
            ResIterator subjects = resultSet.getResourceModel().listSubjects();
            List<String> listOfTexts = new ArrayList<>();
            List<Resource> listOfSubjects = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            int counter = 0;

            while (subjects.hasNext()) {
                Resource subject = subjects.next();
                StmtIterator statements = subject.listProperties();

                while (statements.hasNext()) {
                    Statement statement = statements.next();
                    if (!statement.getObject().isLiteral()) {
                        continue;
                    }

                    Literal literal = statement.getObject().asLiteral();
                    if (!(literal.getDatatype() instanceof XSDBaseStringType)) {
                        continue;
                    }

                    String text = literal.getString();
                    listOfTexts.add(text);
                    listOfSubjects.add(subject);
                    sb.append(text).append("<br>");
                    counter++;

                    if (counter >= literalsPerRequest) {
                        addAnnotatedLiteralsToModel(outputModel, listOfTexts, listOfSubjects, sb);
                        listOfTexts.clear();
                        listOfSubjects.clear();
                        sb.setLength(0);
                        counter = 0;
                    }
                }
            }
            if (counter > 0) { // add remaining literals
                addAnnotatedLiteralsToModel(outputModel, listOfTexts, listOfSubjects, sb);
            }
        }
        return createOutputContext(isReplace, inputModel, outputModel);
    }

    private void addAnnotatedLiteralsToModel(Model outputModel, List<String> listOfTexts, List<Resource> listOfSubjects, StringBuilder sb) {
        String annotatedText = annotateObjectLiteral(sb.toString());
        String[] elements = splitAnnotatedText(annotatedText);

        for (int i = 0; i < listOfSubjects.size(); i++) {
            Resource sub = listOfSubjects.get(i);
            String textElement = listOfTexts.get(i);
            String annotatedTextElement = elements[i];
            Resource annotatedResource = createAnnotatedResource(sub, textElement, annotatedTextElement, outputModel);
            outputModel.add(annotatedResource.getModel());
        }
    }


    private Resource createAnnotatedResource(Resource subject, String originalText, String annotatedText, Model model) {
        Resource annotatedResource;
        if (subject.isAnon()){
            annotatedResource = model.createResource();
        } else {
            String newSubjectUri = subject.getURI() + "-annotated-" + DigestUtils.md5Hex(subject.getURI() + originalText);
            annotatedResource = model.createResource(newSubjectUri);
        }

        annotatedResource.addProperty(RDF.type, Termit.ANNOTATION);
        annotatedResource.addProperty(Termit.ORIGINAL_TEXT, originalText);
        annotatedResource.addProperty(Termit.ANNOTATED_TEXT, annotatedText);

        return annotatedResource;
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
