package cz.cvut.spipes.modules;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import cz.cvut.sforms.SFormsVocabularyJena;
import cz.cvut.sforms.model.Question;
import cz.cvut.sforms.transformation.SForms2TextTransformer;
import cz.cvut.sforms.transformation.TextTransformerConfig;
import cz.cvut.sforms.util.FormUtils;
import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.form.JenaFormUtils;
import cz.cvut.spipes.form.JopaPersistenceUtils;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Predicate;

/**
 * For input Q&A models constructs textual view of specified questions. Each textual view represent the question
 * and its sub-questions recursively.
 */
@SPipesModule(label = "construct textual view", comment =
        "For input Q&A models constructs textual view of specified questions. Each textual" +
        " view represent the question and its sub-questions recursively."
)
public class ConstructTextualViewModule extends AnnotatedAbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(ConstructTextualViewModule.class);

    private static final String TYPE_URI = KBSS_MODULE.uri + "construct-textual-view";

    @Parameter(urlPrefix = SML.uri, name = "replace", comment = "Replace context flag. Default value is false.")
    private boolean isReplace = false;

    @Parameter(urlPrefix = FORM_MODULE.uri, name = "serialize-unanswered-questions",
            comment = "If true (default), outputs questions not having answers")
    private boolean isSerializeUnansweredQuestions = true;

    @Parameter(urlPrefix = FORM_MODULE.uri, name = "serialize-answers",
            comment = "If true (default), outputs answers not only questions")
    private boolean isSerializeAnswers = true;

    @Parameter(urlPrefix = FORM_MODULE.uri, name = "process-non-root-questions",
            comment = "If true (default), process all questions, otherwise process only root questions")
    private boolean isProcessNonRootQuestions = true;

    @Parameter(urlPrefix = FORM_MODULE.uri, name = "indentation-string",
            comment = "Indentation string indents subquestion from questions. By default '  ' is used.")
    private String indentationString = "  ";

    @Parameter(urlPrefix = FORM_MODULE.uri, name = "language",
            comment = "Language to be used to retrieve labels of questions. By default 'en' is used.")
    private String language = "en";

    @Override
    ExecutionContext executeSelf() {

        Model inpModel = this.getExecutionContext().getDefaultModel();

        List<Resource> questions = getQuestions(inpModel, isProcessNonRootQuestions);

        LOG.debug("Found {} questions.", questions.size());
        if (LOG.isTraceEnabled()) {
            LOG.trace("Found questions: {}", questions);
        }

        Model constructedModel = ModelFactory.createDefaultModel();

        EntityManagerFactory emf = JopaPersistenceUtils.createEntityManagerFactoryWithMemoryStore(
            FormUtils.SFORMS_MODEL_PACKAGE_NAME,
            language
        );
        EntityManager em = emf.createEntityManager();

        JopaPersistenceUtils.getDataset(em).setDefaultModel(inpModel);

        TextTransformerConfig cfg = new TextTransformerConfig();
        cfg.setSerializeAnswers(this.isSerializeAnswers);
        cfg.setSerializeUnansweredQuestions(this.isSerializeUnansweredQuestions);
        cfg.setIndentationString(this.indentationString);
        SForms2TextTransformer t = new SForms2TextTransformer();

        for (Resource qR: questions) {
            Question q = em.find(Question.class, qR.getURI());

            constructedModel.add(
                qR,
                SFormsVocabularyJena.s_p_textual_view,
                t.serialize(q, cfg)
            );
        }

        return createOutputContext(isReplace, constructedModel);
    }

    public boolean isSerializeUnansweredQuestions() {
        return isSerializeUnansweredQuestions;
    }

    public void setSerializeUnansweredQuestions(boolean serializeUnansweredQuestions) {
        isSerializeUnansweredQuestions = serializeUnansweredQuestions;
    }

    public boolean isProcessNonRootQuestions() {
        return isProcessNonRootQuestions;
    }

    public void setProcessNonRootQuestions(boolean processNonRootQuestions) {
        isProcessNonRootQuestions = processNonRootQuestions;
    }

    public String getIndentationString() {
        return indentationString;
    }

    public void setIndentationString(String indentationString) {
        this.indentationString = indentationString;
    }

    public boolean isReplace() {
        return isReplace;
    }

    public void setReplace(boolean replace) {
        isReplace = replace;
    }

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

    private List<Resource> getQuestions(Model formModel, boolean isProcessNonRootQuestions) {
        Predicate<Resource> isRootQuestion =
            q -> ! formModel.listResourcesWithProperty(
                    SFormsVocabularyJena.s_p_has_related_question, q).hasNext();

        Predicate<Resource> shouldProcessNonRootQuestions = q -> isProcessNonRootQuestions;

        return JenaFormUtils.getQuestions(formModel)
            .filterKeep(shouldProcessNonRootQuestions.or(isRootQuestion)).toList();
    }
}
