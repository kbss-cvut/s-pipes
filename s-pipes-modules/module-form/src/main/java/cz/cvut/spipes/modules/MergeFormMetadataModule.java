package cz.cvut.spipes.modules;


import cz.cvut.sforms.SformsVocabularyJena;
import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.form.JenaFormUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Inputs are sample form and Q&A model. Questions from both models are remapped to new IRIs based on
 * question origin combined with executionId. New question instances are created using questionInstanceTemplate
 * which defaults to "doc:question-{_questionOriginHash}-{_executionId}".
 */
public class MergeFormMetadataModule extends AnnotatedAbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(MergeFormMetadataModule.class);
    private static final Random RANDOM = new Random();

    private static final String TYPE_URI = KBSS_MODULE.uri + "merge-form-metadata";
    private static final String QUESTION_ORGIN_HASH_VAR = "{_questionOriginHash}";
    private static final String EXECUTION_ID_VAR = "{_executionId}";

    @Parameter(urlPrefix = SML.uri, name = "replace")
    private boolean isReplace = false;

    @Parameter(name = "execution-id")
    private String executionId = DigestUtils.md5Hex(Long.toString(RANDOM.nextLong()));

    @Parameter(name = "question-instance-template")
    private String questionInstanceTemplate =
        SformsVocabularyJena.s_c_question.toString()
            + "-"
            + QUESTION_ORGIN_HASH_VAR
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
                    .replace(QUESTION_ORGIN_HASH_VAR, originHash)
                    .replace(EXECUTION_ID_VAR, executionId);
                if (!q.equals(newQuestionUrl)) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Renaming questions {} -> {}", q, newQuestionUrl);
                    }
                    ResourceUtils.renameResource(q, newQuestionUrl);
                }
            }
        );

        if (isReplace) {
            return ExecutionContextFactory.createContext(constructedModel);
        } else {
            return ExecutionContextFactory.createContext(ModelFactory.createUnion(constructedModel, inpModel));
        }
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
