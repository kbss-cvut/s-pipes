package cz.cvut.spipes.modules;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class PossibleValuesQueryProcessor {

    private static final Logger log = LoggerFactory.getLogger(PossibleValuesQueryProcessor.class);
    String possibleValuesQuery;
    String retrievedPossibleValues;
    Model possibleValuesModel;
    List<Resource> possibleValueResources;

    List<Resource> questions = new LinkedList<>();

    PossibleValuesQueryProcessor(String possibleValuesQuery) {
        this.possibleValuesQuery = possibleValuesQuery;
        this.retrievedPossibleValues = fetchPossibleValues(possibleValuesQuery);
        if (this.retrievedPossibleValues != null) {
            this.possibleValuesModel = convertRetrievedValuesToModel(this.retrievedPossibleValues);
            possibleValueResources = possibleValuesModel.listSubjects().toList();
        }
    }

    private String fetchPossibleValues(String possibleValueQuery) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, "text/turtle");
        final HttpEntity<Object> entity = new HttpEntity<>(null, headers);
        final URI urlWithQuery = URI.create(possibleValueQuery);
        RestTemplate restTemplate = new RestTemplate();
        try {
            final ResponseEntity<String> result = restTemplate.exchange(urlWithQuery, HttpMethod.GET, entity,
                String.class);
            return result.getBody();
        } catch (Exception e) {
            log.error("Error when requesting remote data, url: {}.", urlWithQuery, e);
        }
        return null;
    }

    private Model convertRetrievedValuesToModel(String possibleValues) {
        InputStream pvIS = new ByteArrayInputStream(possibleValues.getBytes());
        return ModelFactory.createDefaultModel().read(pvIS, null, FileUtils.langTurtle);
    }

    public void addQuestion(Resource question) {
        questions.add(question);
    }

    public int getPossibleValuesCount() {
        return possibleValueResources.size();
    }

    public List<Resource> getPossibleValueResources() {
        return Collections.unmodifiableList(possibleValueResources);
    }

    public List<Resource> getRelatedQuestions() {
        return Collections.unmodifiableList(questions);
    }

    public Model getPossibleValuesModel() {
        return possibleValuesModel;
    }
}
