package cz.cvut.sempipes.transform;

import cz.cvut.sforms.VocabularyJena;
import cz.cvut.sforms.model.Answer;
import cz.cvut.sforms.model.Question;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static cz.cvut.sempipes.transform.SPipesUtil.isSPipesTerm;

public class TransformerImpl implements Transformer {

    private static final Logger LOG = LoggerFactory.getLogger(TransformerImpl.class);

    @Override
    public Question script2Form(Model script, Resource module, Resource moduleType) {

        Question formRootQ = new Question();
        initializeQuestionUri(formRootQ);
        formRootQ.setLabel("Module of type " + moduleType.getProperty(RDFS.label).getString());
        formRootQ.setOrigin(toUri(module));
        Set<String> rootLayoutClass = new HashSet<>();
        rootLayoutClass.add("form");
        formRootQ.setLayoutClass(rootLayoutClass);

        Question wizardStepQ = new Question();
        initializeQuestionUri(wizardStepQ);
        wizardStepQ.setLabel("Module configuration");
        wizardStepQ.setOrigin(toUri(module));
        Set<String> wizardStepLayoutClass = new HashSet<>();
        wizardStepLayoutClass.add("wizard-step");
        wizardStepLayoutClass.add("section");
        wizardStepQ.setLayoutClass(wizardStepLayoutClass);

        List<Question> subQuestions = new LinkedList<>();
        Question labelQ = null;
        final Question idQ = new Question();
        initializeQuestionUri(idQ);
        idQ.setOrigin(URI.create(RDFS.Resource.getURI()));
        idQ.getAnswers().add(
                new Answer() {{
                    setCodeValue(URI.create(module.getURI()));
                }}
        );

        Set<Resource> processedPredicates = new HashSet<>();

        Map<OriginPair<URI, URI>, Statement> origin2st = getOrigin2StatementMap(module);

        for (Map.Entry<OriginPair<URI, URI>, Statement> e : origin2st.entrySet()) {
            OriginPair<URI, URI> key = e.getKey();
            Statement st = e.getValue();

            Resource p = st.getPredicate();

            processedPredicates.add(p);
            Question subQ = createQuestion(p);

            Answer a = getAnswer(st.getObject());
            a.setOrigin(key.a);

            subQ.getAnswers().add(a);
            subQ.setOrigin(key.q);

            if (RDFS.label.equals(st.getPredicate())) {
                labelQ = subQ;
            }

            subQuestions.add(subQ);
        }

        List<Statement> typeDefinitionStatements = moduleType.listProperties().filterKeep(
                st -> st.getPredicate().hasURI(VocabularyJena.s_p_constraint.getURI())).toList();
        for (Statement st : typeDefinitionStatements) {
            Resource p = st.getObject().asResource().getPropertyResourceValue(VocabularyJena.s_p_predicate_A);

            if (processedPredicates.contains(p)) {
                continue;
            }

            Question subQ = createQuestion(p);

            subQ.setOrigin(URI.create(p.getURI()));

            subQuestions.add(subQ);
        }


        final Question lQ = labelQ;
//        labelQ.setPrecedingQuestions(Collections.singleton(idQ));
//        subQuestions.stream()
//                .filter(q -> q != lQ)
//                .filter(q -> q != idQ)
//                .forEach(
//                        q -> q.setPrecedingQuestions(Collections.singleton(lQ)));

        subQuestions.add(idQ);

        wizardStepQ.setSubQuestions(new HashSet<>(subQuestions));
        formRootQ.setSubQuestions(Collections.singleton(wizardStepQ));
        return formRootQ;
    }

    @Override
    public Model form2Script(Model inputScript, Question form) {

        Model outputScript = ModelFactory.createDefaultModel();
        outputScript.add(inputScript);

        Resource module = outputScript.getResource(form.getOrigin().toString());

        Map<OriginPair<URI, URI>, Statement> questionStatements = getOrigin2StatementMap(module);
        form.getSubQuestions().forEach((q) -> {
            OriginPair<URI, URI> originPair = new OriginPair<>(q.getOrigin(), getAnswer(q).map(Answer::getOrigin).orElse(null));
            Statement s = questionStatements.get(originPair);
            if (Objects.nonNull(s)) {
                outputScript.remove(s);
            }
            RDFNode answerNode = getAnswerNode(getAnswer(q).orElse(null));
            if (answerNode != null) {
                outputScript.add(s.getSubject(), s.getPredicate(), answerNode);
            }
        });

        return outputScript;
    }

    private Optional<Answer> getAnswer(Question q) {
        if (q.getAnswers() == null || q.getAnswers().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(q.getAnswers().iterator().next());
    }

    private RDFNode getAnswerNode(Answer a) {
        if (a == null) {
            return null;
        }
        if (a.getCodeValue() != null) {
            return ResourceFactory.createResource(a.getCodeValue().toString());
        }
        if (a.getTextValue() != null) {
            return ResourceFactory.createStringLiteral(a.getTextValue());
        }
        return null;
    }

    private Answer getAnswer(RDFNode node) {
        Answer a = new Answer();
        if (node.isURIResource()) {
            a.setCodeValue(URI.create(node.asResource().getURI()));
        }
        else if (node.isLiteral()) {
            a.setTextValue(node.asLiteral().getString());
        }
        else {
            throw new IllegalArgumentException("RDFNode " + node + " is wrong");
        }
        return a;
    }

    private Map<OriginPair<URI, URI>, Statement> getOrigin2StatementMap(Resource module) {
        return module.listProperties()
                .filterDrop(st -> isSPipesTerm(st.getPredicate()))
                .toList().stream()
                .collect(Collectors.toMap(st -> new OriginPair<>(createQuestionOrigin(st), createAnswerOrigin(st)), st -> st));
    }

    private URI toUri(Resource resource) {
        return URI.create(resource.toString());
    }

    private URI createQuestionOrigin(Statement statement) {
        return URI.create(statement.getPredicate().toString());
    }

    private URI createAnswerOrigin(Statement statement) {
        return URI.create(VocabularyJena.s_p_has_answer_origin.toString() +
                "/" + createMd5Hash(statement.getObject().toString()));
    }

    private String createMd5Hash(String text) {
        return DigestUtils.md5Hex(text);
    }

    private void initializeQuestionUri(Question q) {
        q.setUri(URI.create(VocabularyJena.s_c_question + "-" + UUID.randomUUID().toString()));
    }

    private Question createQuestion(Resource property) {
        Question q = new Question();
        initializeQuestionUri(q);
        q.setLabel(property.getURI());
        Statement labelSt = property.getProperty(RDFS.label);
        if (labelSt != null) {
            q.setDescription(labelSt.getString());
        }
        return q;
    }

    public static class OriginPair<Q, A> {
        public final Q q;
        public final A a;

        public OriginPair(Q q, A a) {
            this.q = q;
            this.a = a;
        }
    }
}
