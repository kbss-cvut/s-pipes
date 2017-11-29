package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.VocabularyJena;
import cz.cvut.sempipes.constants.KBSS_MODULE;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConstructFormMetadataModule extends AnnotatedAbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(ConstructFormMetadataModule.class);

    private static final String TYPE_URI = KBSS_MODULE.uri + "construct-form-metadata";
    private static final String TYPE_PREFIX = TYPE_URI + "/";
    private static final String PATH_SEPARATOR = ",";
    private static final String INSTANCE_TYPE_SEPARATOR = "|";
    // TODO remove prefix
    private static final String SML_PREFIX = "http://topbraid.org/sparqlmotionlib#";

    /**
     * URL of the RDF4J repository.
     */
    @Parameter(urlPrefix = SML_PREFIX, name = "replace")
    private boolean isReplace = false;

    private enum Origin {
        QUESTION_ORIGIN("QO"),
        ANSWER_ORIGIN("A0");

        String abbr;

        Origin(String abbr) {
            this.abbr = abbr;
        }

        @Override
        public String toString() {
            return abbr;
        }
    }


    @Override
    ExecutionContext executeSelf() {

        Model inpModel = this.getExecutionContext().getDefaultModel();

        List<Resource> rootQuestions = getRootQuestions(inpModel);

        LOG.debug("Found {} root questions.", rootQuestions.size());
        if (LOG.isTraceEnabled()) {
            LOG.debug("Found root questions: {}", rootQuestions);
        }

        Model constructedModel = ModelFactory.createDefaultModel();

        rootQuestions.forEach(
            q -> processFormEntity(q, null, constructedModel)
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

    private void processFormEntity(final Resource formEntity, final String parentPath, Model constructedModel) {

        String path = constructPath(parentPath, formEntity);
        String pathId = getPathId(path);

        getSubEntities(formEntity).forEach(
            e -> processFormEntity(e, path, constructedModel)
        );

        constructedModel.add(formEntity, VocabularyJena.s_p_has_origin_path, path);
        constructedModel.add(formEntity, VocabularyJena.s_p_has_origin_path_id, pathId);
    }

    private String getPathId(Resource question) {
        return question
            .getProperty(VocabularyJena.s_p_has_origin_path)
            .getObject()
            .asLiteral()
            .getString();
    }

    @NotNull
    private String constructPath(@Nullable String parentPath, @NotNull Resource formEntity) {

        StringBuilder sb = new StringBuilder();

        if (parentPath != null) {
            sb.append(parentPath).append(PATH_SEPARATOR);
        }

        return sb
            .append(getFormEntityOrigin(formEntity))
            .append(INSTANCE_TYPE_SEPARATOR)
            .append(getFormEntityOriginType(formEntity))
            .toString();
    }

    private String getPathId(String path) {
        return DigestUtils.md5Hex(path);
    }


    private Origin getFormEntityOriginType(Resource formEntity) {
        if (isQuestion(formEntity)) {
            return Origin.QUESTION_ORIGIN;
        }
        if (isAnswer(formEntity)) {
            return Origin.ANSWER_ORIGIN;
        }
        throw new IllegalArgumentException("Provided resource " + formEntity + " is not a form entity.");
    }


    private Resource getFormEntityOrigin(Resource formEntity) {
        if (isQuestion(formEntity)) {
            return getQuestionOrigin(formEntity);
        }
        if (isAnswer(formEntity)) {
            return getAnswerOrigin(formEntity);
        }
        throw new IllegalArgumentException("Provided resource " + formEntity + " is not a form entity.");
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

    private static Resource getOrigin(Resource formEntity) {
        if (isQuestion(formEntity)) {
            return getQuestionOrigin(formEntity);
        }
        if (isAnswer(formEntity)) {
            return getAnswerOrigin(formEntity);
        }
        throw new IllegalArgumentException("Provided resource " + formEntity + " is not a form entity.");
    }

    private static Resource getQuestionOrigin(Resource formEntity) {
        return formEntity.getPropertyResourceValue(VocabularyJena.s_p_has_question_origin);
    }

    private static Resource getAnswerOrigin(Resource formEntity) {
        return formEntity.getPropertyResourceValue(VocabularyJena.s_p_has_answer_origin);
    }


    private static boolean isAnswer(Resource formEntity) {
        return formEntity.hasProperty(RDF.type, VocabularyJena.s_c_answer);
    }

    private static boolean isQuestion(Resource formEntity) {
        return formEntity.hasProperty(RDF.type, VocabularyJena.s_c_question);
    }

    private static List<Resource> getRelatedQuestions(Resource question) {
        return question.listProperties(VocabularyJena.s_p_has_related_question).mapWith(st -> st.getObject().asResource()).toList();
    }

    private static List<Resource> getAnswers(Resource question) {
        return question.listProperties(VocabularyJena.s_p_has_answer).mapWith(st -> st.getObject().asResource()).toList();
    }

    private static List<Resource> getSubEntities(Resource formEntity) {
        List<Resource> resList = new LinkedList<>();

        if (isQuestion(formEntity)) {
            resList.addAll(getRelatedQuestions(formEntity));
        }

        resList.addAll(getAnswers(formEntity));
        return resList;
    }

    private static boolean isLeafFormEntity(Resource formEntity) {

        if (formEntity.hasProperty(VocabularyJena.s_p_has_related_question)) {
            return false;
        }
        if (formEntity.hasProperty(VocabularyJena.s_p_has_answer)) {
            return false;
        }
        return true;
    }


    private static Property getJenaProperty(String property) {
        return ResourceFactory.createProperty(property);
    }


    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

}
