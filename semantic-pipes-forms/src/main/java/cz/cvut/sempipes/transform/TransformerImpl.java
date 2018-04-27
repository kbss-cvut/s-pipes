package cz.cvut.sempipes.transform;

import cz.cvut.sforms.FormUtils;
import cz.cvut.sforms.Vocabulary;
import cz.cvut.sforms.VocabularyJena;
import cz.cvut.sforms.model.Answer;
import cz.cvut.sforms.model.Question;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.arq.ARQ2SPIN;
import org.topbraid.spin.system.SPINModuleRegistry;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static cz.cvut.sempipes.transform.SPipesUtil.isSPipesTerm;

public class TransformerImpl implements Transformer {

    private static final Logger LOG = LoggerFactory.getLogger(TransformerImpl.class);

    public TransformerImpl() {
        SPINModuleRegistry.get().init();
    }

    @Override
    public Question script2Form(Model script, Resource module, Resource moduleType) {

        if (!URI.create(module.getURI()).isAbsolute()) {
            throw new IllegalArgumentException("Module uri '" + module.getURI() + "' is not absolute.");
        }
        if (!URI.create(moduleType.getURI()).isAbsolute()) {
            throw new IllegalArgumentException("Module type uri '" + module.getURI() + "' is not absolute.");
        }

        Question formRootQ = new Question();
        initializeQuestionUri(formRootQ);
        formRootQ.setLabel("Module of type " + moduleType.getProperty(RDFS.label).getString());
        formRootQ.setOrigin(toUri(module));
        formRootQ.setLayoutClass(Collections.singleton("form"));

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
        idQ.setLabel("URI");
        idQ.setOrigin(URI.create(RDFS.Resource.getURI()));
        Answer idAnswer = new Answer();
        idAnswer.setCodeValue(URI.create(module.getURI()));
        idQ.setAnswers(Collections.singleton(idAnswer));

        Set<Resource> processedPredicates = new HashSet<>();

        Map<OriginPair<URI, URI>, Statement> origin2st = getOrigin2StatementMap(module);

        for (Map.Entry<OriginPair<URI, URI>, Statement> e : origin2st.entrySet()) {
            OriginPair<URI, URI> key = e.getKey();
            Statement st = e.getValue();

            Resource p = st.getPredicate();

            processedPredicates.add(p);
            Question subQ = createQuestion(p);

            subQ.setProperties(extractQuestionMetadata(st));

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

            subQ.setProperties(extractQuestionMetadata(st));

            subQ.setOrigin(URI.create(p.getURI()));
            subQ.setAnswers(Collections.singleton(new Answer()));

            subQuestions.add(subQ);
        }


        final Question lQ;
        if (labelQ == null) {
            lQ = new Question();
            initializeQuestionUri(lQ);
            lQ.setOrigin(URI.create(RDFS.label.getURI()));
            lQ.setLabel(RDFS.label.getURI());
            lQ.setAnswers(Collections.singleton(new Answer()));
            subQuestions.add(lQ);
        }
        else
            lQ = labelQ;
        lQ.setPrecedingQuestions(Collections.singleton(idQ));
        subQuestions.stream()
                .filter(q -> q != lQ)
                .filter(q -> q != idQ)
                .forEach(
                        q -> q.setPrecedingQuestions(Collections.singleton(lQ)));

        subQuestions.add(idQ);

        wizardStepQ.setSubQuestions(new HashSet<>(subQuestions));
        formRootQ.setSubQuestions(Collections.singleton(wizardStepQ));
        return formRootQ;
    }

    @Override
    public Model form2Script(Model inputScript, Question form, String moduleType) {

        Model outputScript = ModelFactory.createDefaultModel();
        outputScript.add(inputScript);

        Resource module = outputScript.getResource(form.getOrigin().toString());

        Question uriQ = findUriQ(form);
        URI newUri = new ArrayList<>(uriQ.getAnswers()).get(0).getCodeValue();

        if (module.listProperties().hasNext()) {
            Map<OriginPair<URI, URI>, Statement> questionStatements = getOrigin2StatementMap(module); // Created answer origin is different from the actual one
            findRegularQ(form).forEach((q) -> {
                OriginPair<URI, URI> originPair = new OriginPair<>(q.getOrigin(), getAnswer(q).map(Answer::getOrigin).orElse(null));
                Statement s = questionStatements.get(originPair);
                if (Objects.nonNull(s)) {
                    outputScript.remove(s);
                }
                if (isSupportedAnon(q)) {
                    Query query = AnonNodeTransformer.parse(q, inputScript);
                    org.topbraid.spin.model.Query spinQuery = ARQ2SPIN.parseQuery(query.serialize(), inputScript);
                    outputScript.add(spinQuery.getModel());
                }
                else {
                    RDFNode answerNode = getAnswerNode(getAnswer(q).orElse(null));
                    if (answerNode != null) {
                        outputScript.add(s.getSubject(), s.getPredicate(), answerNode);
                    }
                }
            });
        }
        else {
            outputScript.add(outputScript.getResource(newUri.toString()), RDF.type, outputScript.getResource(moduleType));
            outputScript.add(outputScript.getResource(newUri.toString()), RDF.type, outputScript.getResource(Vocabulary.s_c_Modules_A));
            findRegularQ(form).forEach((q) -> {
                RDFNode answerNode = getAnswerNode(getAnswer(q).orElse(null));
                if (answerNode != null) {
                    outputScript.add(outputScript.getResource(newUri.toString()), new PropertyImpl(q.getOrigin().toString()), answerNode);
                }
            });
        }

        ResourceUtils.renameResource(module, newUri.toString());

        return outputScript;
    }

    private Question findUriQ(Question root) {
        Optional<Question> uriQ = FormUtils.flatten(root).stream().filter((q) -> RDFS.Resource.getURI().equals(q.getOrigin().toString())).findFirst();
        if (uriQ.isPresent())
            return uriQ.get();
        throw new IllegalArgumentException("Root question has no subquestion that maps to URI");
    }

    private Set<Question> findRegularQ(Question root) {
        return FormUtils.flatten(root).stream()
                .filter((q) -> q.getSubQuestions() == null || q.getSubQuestions().isEmpty())
                .filter((q) -> !RDFS.Resource.getURI().equals(q.getOrigin().toString()))
                .collect(Collectors.toSet());
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
        else if (node.isAnon()) {
            a.setTextValue(AnonNodeTransformer.serialize(node));
        }
        else {
            throw new IllegalArgumentException("RDFNode " + node + " should be a literal, a URI resource or an anonymous node of a known type");
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
        if (!statement.getObject().isAnon())
            return URI.create(VocabularyJena.s_c_answer_origin.toString() +
                    "/" + createMd5Hash(statement.getObject().toString()));
        return URI.create(VocabularyJena.s_c_answer_origin.toString() +
                "/" + createMd5Hash(AnonNodeTransformer.serialize(statement.getObject())));
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

    private Map<String, Set<String>> extractQuestionMetadata(Statement st) {
        Map<String, Set<String>> p = new HashMap<>();
        if (st.getPredicate().hasProperty(RDFS.range))
            p.put(Vocabulary.s_p_has_answer_value_type, Collections.singleton(st.getPredicate().getProperty(RDFS.range).getObject().asResource().getURI()));
        Model m = extractModel(st);
        p.put(Vocabulary.s_p_has_origin_context, Collections.singleton(m.listStatements(null, RDF.type, OWL.Ontology).next().getSubject().getURI()));
        return p;
    }

    public static class OriginPair<Q, A> {
        public final Q q;
        public final A a;

        public OriginPair(Q q, A a) {
            this.q = q;
            this.a = a;
        }

        @Override
        public int hashCode() {
            if (q == null)
                return a.hashCode();
            if (a == null)
                return q.hashCode();
            return (q.hashCode() + a.hashCode()) % 21;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof OriginPair))
                return false;
            OriginPair p = (OriginPair) o;
            return Objects.equals(q, p.q) && Objects.equals(a, p.a);
        }
    }

    private boolean isSupportedAnon(Question q) {
        if (q.getProperties().containsKey(Vocabulary.s_p_has_answer_value_type)) {
            Set<String> types = q.getProperties().get(Vocabulary.s_p_has_answer_value_type);
            return types.contains(Vocabulary.s_c_Ask) ||
                    types.contains(Vocabulary.s_c_Construct) ||
                    types.contains(Vocabulary.s_c_Describe) ||
                    types.contains(Vocabulary.s_c_Select);
        }
        return false;
    }

    Model extractModel(Statement st) {
        Model model = st.getModel(); // Iterate through subgraphs and find model defining st and return it (or IRI)
        return find(st, model.getGraph(), Optional.empty()).orElse(null);
    }

    private Optional<Model> find(Statement st, Graph graph, Optional<Model> res) {
        if (res.isPresent())
            return res;
        Model m = ModelFactory.createModelForGraph(graph);
        if (m.contains(st))
            return find(st, graph, Optional.of(m));
        if (m instanceof OntModel) {
            OntModel ontM = (OntModel) m;
            Optional<Optional<Model>> o = ontM.getSubGraphs().stream().map(g -> find(st, g, res)).filter(Optional::isPresent).findFirst();
            if (o.isPresent())
                return o.get();
        }
        return Optional.empty();
    }
}
