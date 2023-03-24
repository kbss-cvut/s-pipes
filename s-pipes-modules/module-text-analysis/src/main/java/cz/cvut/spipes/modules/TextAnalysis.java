package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
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

import java.io.IOException;
import java.util.Iterator;

public class TextAnalysis extends AnnotatedAbstractModule{

    private static final Logger LOG = LoggerFactory.getLogger(TextAnalysis.class);
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

            while (resultSet.hasNext()) {
                QuerySolution solution = resultSet.next();

                // iterate over all variables in the query solution
                Iterator<String> varNames = solution.varNames();

                while (varNames.hasNext()) {
                    String varName = varNames.next();
                    RDFNode node = solution.get(varName);
                    if (node.isResource()) {
                        Resource subject = node.asResource();
                        StmtIterator stmtIterator = subject.listProperties();
                        while (stmtIterator.hasNext()) {

                            Statement stmt = stmtIterator.next();
                            Property predicate = stmt.getPredicate();
                            RDFNode object = stmt.getObject();

                            if (object.isLiteral() && object.asLiteral().getDatatype() instanceof XSDBaseStringType) {
                                Resource annotationResource = outputModel.createResource();
                                Element el = Jsoup.parse(object.asLiteral().toString());

                                String text = el.text();
                                String annotatedLiteral = annotateObjectLiteral(text);

                                annotationResource.addProperty(RDF.type, Termit.ANNOTATION);
                                annotationResource.addLiteral(Termit.ORIGINAL_TEXT, text);
                                annotationResource.addLiteral(Termit.ANNOTATED_TEXT, annotatedLiteral);

                                Statement annotatedStatement = outputModel.createStatement(subject, predicate, annotatedLiteral);
                                outputModel.add(annotatedStatement);
                            }
                        }
                    }
                }
            }
        }
        return createOutputContext(isReplace, inputModel, outputModel);
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
        return TYPE_URI + "/";
    }
}
