package cz.cvut.spipes.modules;


import cz.cvut.sforms.SFormsVocabularyJena;
import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.form.JenaFormUtils;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

@SPipesModule(label = "merge form metadata", comment =
        "Merges form metadata. Inputs are sample form and Q&A model. Questions from both models are remapped to new" +
        " IRIs based on question origin combined with executionId. New question instances are created using" +
        " question-instance-template property which defaults to \"doc:question-{_questionOriginHash}-{_executionId}\"."
)
public class MergeFormMetadataModule extends AnnotatedAbstractModule {

    private static final Logger log = LoggerFactory.getLogger(MergeFormMetadataModule.class);
    private static final Random RANDOM = new Random();

    private static final String TYPE_URI = KBSS_MODULE.uri + "merge-form-metadata";
    // TODO use official uri templates
    private static final String QUESTION_ORIGIN_HASH_VAR = "{_questionOriginHash}";
    private static final String EXECUTION_ID_VAR = "{_executionId}";

    @Parameter(iri = SML.replace, comment = "Specifies whether a module should overwrite triples" +
        " from its predecessors. When set to true (default is false), it prevents" +
        " passing through triples from the predecessors.")
    private boolean isReplace = false;

    @Parameter(iri = KBSS_MODULE.uri + "execution-id", comment = "Execution id that will be used to construct question IRIs")
    private String executionId = DigestUtils.md5Hex(Long.toString(RANDOM.nextLong()));

    @Parameter(iri = KBSS_MODULE.uri + "question-instance-template", comment = "URL Template to create URL for question instances. " +
            "Default is 'http://onto.fel.cvut.cz/ontologies/documentation/question-{_questionOriginHash}-{_executionId}'")
    private String questionInstanceTemplate =
        SFormsVocabularyJena.s_c_question.toString()
            + "-"
            + QUESTION_ORIGIN_HASH_VAR
            + "-"
            + EXECUTION_ID_VAR;


    @Override
    ExecutionContext executeSelf() {

        Model inpModel = this.getExecutionContext().getDefaultModel();

        Model constructedModel = ModelFactory.createDefaultModel();
        constructedModel.add(inpModel);

        JenaFormUtils.getQuestions(constructedModel).forEachRemaining(
            q -> {
                String originHash = DigestUtils.md5Hex(JenaFormUtils.getQuestionOrigin(q).toString());
                String newQuestionUrl = questionInstanceTemplate
                    .replace(QUESTION_ORIGIN_HASH_VAR, originHash)
                    .replace(EXECUTION_ID_VAR, executionId);
                if (!q.getURI().equals(newQuestionUrl)) {
                    if (log.isTraceEnabled()) {
                        log.trace("Renaming questions {} -> {}", q, newQuestionUrl);
                    }
                    ResourceUtils.renameResource(q, newQuestionUrl);
                }
            }
        );

        return createOutputContext(isReplace, constructedModel);
    }

    public boolean isReplace() {
        return isReplace;
    }

    public void setReplace(boolean replace) {
        isReplace = replace;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

}
