package cz.cvut.spipes.modules;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import cz.cvut.kbss.jopa.model.query.TypedQuery;
import cz.cvut.sforms.Vocabulary;
import cz.cvut.sforms.model.Question;
import cz.cvut.sforms.util.FormUtils;
import cz.cvut.spipes.VocabularyJena;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.form.JopaPersistenceUtils;
import java.net.URI;
import java.util.List;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConstructTextSerializationModule extends AnnotatedAbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(ConstructFormMetadataModule.class);

    private static final String TYPE_URI = FORM_MODULE.uri + "construct-text-serialization";
    private static final String TYPE_PREFIX = TYPE_URI + "/";
    private static final String PATH_SEPARATOR = ",";
    private static final String INSTANCE_TYPE_SEPARATOR = "|";

    @Parameter(urlPrefix = SML.uri, name = "replace")
    private boolean isReplace = false;



    @Override
    ExecutionContext executeSelf() {

        Model inpModel = this.getExecutionContext().getDefaultModel();

        List<Resource> rootQuestions = getRootQuestions(inpModel);

        LOG.debug("Found {} root questions.", rootQuestions.size());
        if (LOG.isTraceEnabled()) {
            LOG.trace("Found root questions: {}", rootQuestions);
        }

        Model constructedModel = ModelFactory.createDefaultModel();

        EntityManagerFactory emf = JopaPersistenceUtils.createEntityManagerFactoryWithMemoryStore(FormUtils.SFORMS_MODEL_PACKAGE_NAME);
        EntityManager em = emf.createEntityManager();

        JopaPersistenceUtils.getDataset(em).setDefaultModel(inpModel);


        TypedQuery<Question> query = em.createNativeQuery("SELECT ?s WHERE { ?s a ?type}", Question.class)
            .setParameter("type", URI.create(Vocabulary.s_c_question));

        List<Question> questions = query.getResultList();

//        rootQuestions.forEach(
//            q -> processFormEntity(q, null, constructedModel)
//        );

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

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

    private List<Resource> getRootQuestions(Model formModel) {
        return formModel.listSubjects()
            .filterKeep(
                subj -> subj.hasProperty(
                    RDF.type,
                    VocabularyJena.s_c_question
                )
            )
            .filterDrop(
                subj -> formModel.listResourcesWithProperty(
                    VocabularyJena.s_p_has_related_question, subj).hasNext()
            ).toList();
    }
}
