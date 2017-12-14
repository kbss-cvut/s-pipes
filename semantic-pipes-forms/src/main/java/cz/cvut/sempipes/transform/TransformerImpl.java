package cz.cvut.sempipes.transform;

import cz.cvut.sempipes.VocabularyJena;
import cz.cvut.sempipes.model.qam.Answer;
import cz.cvut.sempipes.model.qam.Question;
import cz.cvut.sempipes.model.spipes.Module;
import cz.cvut.sempipes.model.spipes.ModuleType;
import static cz.cvut.sempipes.transform.SPipesUtil.isSPipesTerm;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransformerImpl implements Transformer {

    private static final Logger LOG = LoggerFactory.getLogger(TransformerImpl.class);

    @Override
    public Question script2Form(Model script, Resource module, Resource moduleType) {

        Question formRootQ = new Question();

        formRootQ.setLabel("Module of type " + moduleType.getProperty(RDFS.label).getString());
        formRootQ.setOrigin(toUri(module));
        // layout section
        // layout wizzard-step

        List<Statement> valueStatements = module.listProperties().filterDrop(st -> isSPipesTerm(st.getPredicate())).toList();
        List<Question> subQuestions = new LinkedList<>();
        Question labelQ = null;
        final Question idQ = new Question();
        idQ.setOrigin(URI.create(RDFS.Resource.getURI()));
        idQ.getAnswers().add(
            new Answer() {{
                setCodeValue(URI.create(module.getURI()));
            }}
        );

        Set<Resource> processedPredicates = new HashSet<>();

        Map<URI, Statement> origin2st = getOrigin2StatementMap(module);

        for (Map.Entry<URI, Statement> e : origin2st.entrySet()) {
            URI key = e.getKey();
            Statement st = e.getValue();

            Resource p = st.getPredicate();

            processedPredicates.add(p);
            Question subQ = createQuestion(p);

            subQ.getAnswers().add(getAnswer(st.getObject()));
            subQ.setOrigin(key);

            if (RDFS.label.equals(st.getPredicate())) {
                labelQ = subQ;
            }

            subQuestions.add(subQ);
        }

        List<Statement> typeDefinitionStatements = moduleType.listProperties().filterKeep(
            st -> st.getPredicate().hasURI(VocabularyJena.s_p_constraint.getURI())).toList();
        for (Statement st : typeDefinitionStatements) {
            Resource p = st.getObject().asResource().getPropertyResourceValue(VocabularyJena.s_p_predicate);

            if (processedPredicates.contains(p)) {
                continue;
            }

            Question subQ = createQuestion(p);

            subQ.setOrigin(URI.create(p.getURI()));

            subQuestions.add(subQ);
        }


        final Question lQ = labelQ;
        // preceding question
        labelQ.setPrecedingQuestions(Collections.singleton(idQ));
        subQuestions.stream()
            .filter(q -> q != lQ)
            .filter(q -> q != idQ)
            .forEach(
                q -> q.setPrecedingQuestions(Collections.singleton(lQ)));

        subQuestions.add(idQ);

        // populate questions based on module values
        formRootQ.setSubQuestions(new HashSet<>(subQuestions));
        return formRootQ;
    }

    @Override
    public Model form2Script(Model inputScript, Question form) {

        Model outputScript = ModelFactory.createDefaultModel();
        outputScript.add(inputScript);

        Resource module = outputScript.getResource(form.getOrigin().toString());

        Map<URI, Statement> hashes = getOrigin2StatementMap(module);
        form.getSubQuestions().forEach((q) -> {
            Statement s = hashes.get(q.getOrigin());
            if (Objects.nonNull(s)) {
                outputScript.remove(s);
            }
            RDFNode answer = getAnswerNode(getAnswer(q));
            if (answer != null) {
                outputScript.add(s.getSubject(), s.getPredicate(), answer);
            }
        });

        return outputScript;
    }


    private Answer getAnswer(Question q) {
        if (q.getAnswers() == null || q.getAnswers().isEmpty()) {
            return null;
        }
        return new LinkedList<>(q.getAnswers()).get(0);
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
        } else if (node.isLiteral()) {
            a.setTextValue(node.asLiteral().getString());
        } else {
            throw new IllegalArgumentException("RDFNode " + node + " is wrong");
        }
        return a;
    }

    private Map<URI, Statement> getOrigin2StatementMap(Resource module) {
        return module.listProperties()
            .filterDrop(st -> isSPipesTerm(st.getPredicate()))
            .toList().stream()
            .collect(Collectors.toMap(this::createOrigin, st -> st));
    }

    private URI toUri(Resource resource) {
        return URI.create(resource.toString());
    }

    private URI createOrigin(Statement statement) {
        return URI.create(
            VocabularyJena.s_c_question_origin.toString()
                + "/"
                + createMd5Hash(statement.getPredicate().toString() + statement.getObject().toString())
        );
    }

    private String createMd5Hash(String text) {
        return DigestUtils.md5Hex(text);
    }

    private Question createQuestion(Resource property) {
        Question q = new Question();

        q.setLabel(property.getURI());
        Statement labelSt = property.getProperty(RDFS.label);
        if (labelSt != null) {
            q.setDescription(labelSt.getString());
        }
        return q;
    }


}
