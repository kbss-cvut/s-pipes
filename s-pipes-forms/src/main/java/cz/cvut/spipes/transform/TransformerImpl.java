package cz.cvut.spipes.transform;

import cz.cvut.sforms.SFormsVocabularyJena;
import cz.cvut.sforms.Vocabulary;
import cz.cvut.sforms.model.Answer;
import cz.cvut.sforms.model.PrefixDefinition;
import cz.cvut.sforms.model.Question;
import cz.cvut.sforms.util.FormUtils;
import cz.cvut.spipes.util.JenaUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static cz.cvut.spipes.transform.SPipesUtil.isSPipesTerm;

public class TransformerImpl implements Transformer {

    private final Logger LOG = LoggerFactory.getLogger(TransformerImpl.class);

    @Override
    public Question script2Form(Resource module, Resource moduleType) {
        if (!URI.create(module.getURI()).isAbsolute()) {
            throw new IllegalArgumentException("Module uri '" + module.getURI() + "' is not absolute.");
        }
        if (!URI.create(moduleType.getURI()).isAbsolute()) {
            throw new IllegalArgumentException("Module type uri '" + module.getURI() + "' is not absolute.");
        }

        Question formRootQ = new Question();
        initializeQuestionUri(formRootQ);
        formRootQ.setOrigin(toUri(module));
        formRootQ.setLayoutClass(Collections.singleton("form"));

        Question wizardStepQ = new Question();
        initializeQuestionUri(wizardStepQ);
        wizardStepQ.setLabel("Module of type " + moduleType.getProperty(RDFS.label).getString() + " (" + moduleType.getURI() + ")");
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
        idAnswer.setHash(DigestUtils.sha1Hex(module.getURI()));
        idQ.setAnswers(Collections.singleton(idAnswer));

        Set<Resource> processedPredicates = new HashSet<>();

        Map<OriginPair<URI, URI>, Statement> origin2st = getOrigin2StatementMap(module);

        LOG.info("Creating new form.");
        for (Map.Entry<OriginPair<URI, URI>, Statement> e : origin2st.entrySet()) {
            OriginPair<URI, URI> key = e.getKey();
            Statement st = e.getValue();

            Resource p = st.getPredicate();

            processedPredicates.add(p);

            Question subQ = createQuestion(p);
            subQ.setProperties(extractQuestionMetadata(st));

            if (st.getObject().isAnon() && SPipesUtil.getSPinCommandType(st.getObject().asResource()) != null) {
                subQ.setLayoutClass(Collections.singleton("sparql"));
                subQ.getProperties().put(
                        Vocabulary.s_p_has_answer_value_type,
                        Collections.singleton(SPipesUtil.getSPinCommandType(st.getObject().asResource()).getResource().getURI())
                );
                subQ.setDeclaredPrefix(p.getModel().getNsPrefixMap().entrySet().stream().map(
                        prefix -> new PrefixDefinition(prefix.getKey(), prefix.getValue())).collect(Collectors.toSet())
                );
            }

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
                st -> st.getPredicate().hasURI(SFormsVocabularyJena.s_p_constraint.getURI())).toList();
        for (Statement st : typeDefinitionStatements) {
            Resource p = st.getObject().asResource().getPropertyResourceValue(SFormsVocabularyJena.s_p_predicate_A);

            if (processedPredicates.contains(p)) {
                continue;
            }

            Question subQ = createQuestion(p);

            subQ.setProperties(extractQuestionMetadata(st));

            subQ.setOrigin(URI.create(p.getURI()));
            subQ.setAnswers(Collections.singleton(new Answer()));

            subQuestions.add(subQ);

            processedPredicates.add(p);
        }


        final Question lQ;
        if (labelQ == null) {
            lQ = new Question();
            initializeQuestionUri(lQ);
            lQ.setOrigin(URI.create(RDFS.label.getURI()));
            lQ.setLabel(RDFS.label.getURI());
            lQ.setAnswers(Collections.singleton(new Answer()));
            subQuestions.add(lQ);
        } else
            lQ = labelQ;
        lQ.setPrecedingQuestions(Collections.singleton(idQ));
        subQuestions.stream()
                .filter(q -> q != lQ)
                .filter(q -> q != idQ)
                .forEach(
                        q -> q.setPrecedingQuestions(Collections.singleton(lQ)));

        subQuestions.add(idQ);

        Question ttlQ = new Question();
        initializeQuestionUri(ttlQ);
        ttlQ.setLayoutClass(Collections.singleton("ttl"));
        Answer ttlA = new Answer();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        JenaUtils.writeScript(os, ModelFactory.createDefaultModel().add(module.listProperties()));
        String ttlStr = os.toString();
        ttlA.setTextValue(ttlStr);
        ttlA.setHash(DigestUtils.sha1Hex(ttlStr));

        ttlQ.setAnswers(Collections.singleton(ttlA));

        Question ttlStepQ = new Question();
        initializeQuestionUri(ttlStepQ);
        ttlStepQ.setLabel("TTL");
        ttlStepQ.setOrigin(toUri(module));
        ttlStepQ.setLayoutClass(wizardStepLayoutClass);
        ttlStepQ.setSubQuestions(Collections.singleton(ttlQ));

        HashSet<Question> steps = new HashSet<>();
        steps.add(wizardStepQ);
        steps.add(ttlStepQ);

        wizardStepQ.setSubQuestions(new HashSet<>(subQuestions));
        formRootQ.setSubQuestions(steps);
        return formRootQ;
    }

    @Override
    public Map<String, Model> form2Script(Model inputScript, Question form, String moduleType) {

        Map<String, Model> changed = new HashMap<>();

        LOG.info("origin: " + form.getOrigin().toString());
        Resource module = inputScript.getResource(form.getOrigin().toString());

        Question uriQ = findUriQ(form);
        URI newUri = new ArrayList<>(uriQ.getAnswers()).get(0).getCodeValue();

        Optional<Model> ttlModel = getTTLModel(form);

        boolean ttlChanged = form.getSubQuestions().stream()
                .flatMap(q -> q.getSubQuestions().stream())
                .filter(q -> q.getLayoutClass().stream().anyMatch(s -> s.equals("ttl")))
                .findFirst()
                .map(q -> q.getAnswers().stream().anyMatch(a -> !DigestUtils.sha1Hex(a.getTextValue()).equals(a.getHash())))
                .orElse(false);

        if (module.listProperties().hasNext()) {
            Map<OriginPair<URI, URI>, Statement> questionStatements = getOrigin2StatementMap(module); // Created answer origin is different from the actual one
            findRegularQ(form).forEach((q) -> {
                LOG.info("QUESTION: " + q.toString());
                OriginPair<URI, URI> originPair = new OriginPair<>(q.getOrigin(), getAnswer(q).map(Answer::getOrigin).orElse(null));
                Statement s = questionStatements.get(originPair);
                if (s != null) {
                    final Model m = extractModel(s);
                    final String uri = ((OntModel) inputScript).getBaseModel().listStatements(null, RDF.type, OWL.Ontology).next().getSubject().getURI();
                    if (!changed.containsKey(uri)) {
                        changed.put(uri, ModelFactory.createDefaultModel().add(m instanceof OntModel ? ((OntModel) m).getBaseModel() : m));
                    }
                    final Model changingModel = changed.get(uri);

                    LOG.info("STATEMENT: " + s);
                    RDFNode answerNode = getAnswerNode(getAnswer(q).orElse(null));
                    if (answerNode != null) {
                        if (s.getObject().isAnon()){
                            Statement an = s.getObject().asResource().listProperties().next();
                            LOG.info("ANON STATEMENT: " + an);
                            changingModel.remove(an);
                            changingModel.add(an.getSubject(), an.getPredicate(), answerNode);
                        } else{
                            changingModel.remove(s);
                            //handle only value - should handle other types
                            if(q.getLabel().equals("http://topbraid.org/sparqlmotionlib#value")){
                                Resource subject = m.createResource(newUri.toString());
                                Property predicate = m.createProperty("http://topbraid.org/sparqlmotionlib#value");
                                Resource object = m.createResource();
                                m.add(subject, predicate, object);
                                m.add(object, new PropertyImpl("http://spinrdf.org/sp#varName"), answerNode);
                            } else if(q.getLabel().equals("http://topbraid.org/sparqlmotionlib#constructQuery")){
                                Resource subject = m.createResource(newUri.toString());
                                Property predicate = m.createProperty("http://topbraid.org/sparqlmotionlib#constructQuery");
                                Resource object = m.createResource();
                                m.add(subject, predicate, object);
                                m.add(object, new PropertyImpl("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), ResourceFactory.createResource("http://spinrdf.org/sp#Construct"));
                                m.add(object, new PropertyImpl("http://spinrdf.org/sp#text"), answerNode);
                            } else {
                                changingModel.add(s.getSubject(), s.getPredicate(), answerNode);
                            }
                        }
                    }
                } else {
                    Model m = inputScript;
                    RDFNode answerNode = getAnswerNode(getAnswer(q).orElse(null));
                    handleFormUpdate(answerNode, inputScript, q, newUri);
                    changed.put(((OntModel) inputScript).getBaseModel().listStatements(null, RDF.type, OWL.Ontology).next().getSubject().getURI(), m);
                }
            });
        } else {
            Model m = inputScript;
            m.add(m.getResource(newUri.toString()), RDF.type, m.getResource(moduleType));
            m.add(m.getResource(newUri.toString()), RDF.type, m.getResource(cz.cvut.sforms.Vocabulary.s_c_Modules));
            findRegularQ(form).forEach((q) -> {
                LOG.info("QUESTION_NEW: " + q.toString());
                RDFNode answerNode = getAnswerNode(getAnswer(q).orElse(null));
                handleFormUpdate(answerNode, m, q, newUri);
            });
            changed.put(((OntModel) inputScript).getBaseModel().listStatements(null, RDF.type, OWL.Ontology).next().getSubject().getURI(), m);
        }

        if (ttlChanged) {
            ttlModel.map(m ->
                changed.put(
                    m.listStatements(null, RDF.type, OWL.Ontology).next().getSubject().getURI(),
                    m
                )
            );
        }

        ResourceUtils.renameResource(module, newUri.toString());

        return changed;
    }

    private void handleFormUpdate(RDFNode answerNode, Model m, Question q, URI newUri){
        LOG.info("answerNode: " + answerNode);
        if (answerNode != null) {
            LOG.info("answerNode: " + answerNode.toString());
            if(q.getLabel().equals("http://topbraid.org/sparqlmotionlib#value")){
                Resource subject = m.createResource(newUri.toString());
                Property predicate = m.createProperty("http://topbraid.org/sparqlmotionlib#value");
                Resource object = m.createResource();
                m.add(subject, predicate, object);
                m.add(object, new PropertyImpl("http://spinrdf.org/sp#varName"), answerNode);
            }else if(q.getLabel().equals("http://topbraid.org/sparqlmotionlib#constructQuery")){
                Resource subject = m.createResource(newUri.toString());
                Property predicate = m.createProperty("http://topbraid.org/sparqlmotionlib#constructQuery");
                Resource object = m.createResource();
                m.add(subject, predicate, object);
                m.add(object, new PropertyImpl("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), ResourceFactory.createResource("http://spinrdf.org/sp#Construct"));
                m.add(object, new PropertyImpl("http://spinrdf.org/sp#text"), answerNode);
            }else {
                m.add(m.getResource(newUri.toString()), new PropertyImpl(q.getOrigin().toString()), answerNode);
            }
        }
    }

    @Override
    public Question functionToForm(Model script, Resource function) {
        if (!URI.create(function.getURI()).isAbsolute()) {
            throw new IllegalArgumentException("Function uri '" + function.getURI() + "' is not absolute.");
        }

        Question formRootQ = new Question();
        initializeQuestionUri(formRootQ);
        formRootQ.setLabel("");
        formRootQ.setLayoutClass(Collections.singleton("form"));

        Answer executionId = new Answer();
        executionId.setTextValue(UUID.randomUUID().toString());
        formRootQ.setAnswers(Collections.singleton(executionId));

        Question wizardStepQ = new Question();
        initializeQuestionUri(wizardStepQ);
        wizardStepQ.setLabel("Function call");
        Set<String> wizardStepLayoutClass = new HashSet<>();
        wizardStepLayoutClass.add("wizard-step");
        wizardStepLayoutClass.add("section");
        wizardStepQ.setLayoutClass(wizardStepLayoutClass);

        List<Question> subQuestions = new LinkedList<>();

        Question functionQ = new Question();
        initializeQuestionUri(functionQ);
        functionQ.setLabel("URI");
        functionQ.setDescription("URI of the function that will be called");
        functionQ.setOrigin(URI.create(RDF.uri));
        Answer functionAnswer = new Answer();
        functionAnswer.setTextValue(function.getURI());
        functionQ.setAnswers(Collections.singleton(functionAnswer));

        subQuestions.add(functionQ);

        Property parameterProp = function.getModel().createProperty("http://www.w3.org/ns/shacl#parameter");
        Property pathProp = function.getModel().createProperty("http://www.w3.org/ns/shacl#path");

        StmtIterator parameters = function.listProperties(parameterProp);
        while (parameters.hasNext()) {
            Resource constraint = parameters.nextStatement().getResource();
            Statement st = constraint.getProperty(pathProp);

            Question q = new Question();
            initializeQuestionUri(q);
            q.setLabel(st.getResource().getLocalName());
            q.setProperties(extractQuestionMetadata(st));
            q.setPrecedingQuestions(Collections.singleton(functionQ));
            subQuestions.add(q);
        }

        wizardStepQ.setSubQuestions(new HashSet<>(subQuestions));
        formRootQ.setSubQuestions(Collections.singleton(wizardStepQ));

        return formRootQ;
    }

    private Question findUriQ(Question root) {
        Optional<Question> uriQ =
                FormUtils.flatten(root).stream()
                        .filter(q -> q.getOrigin() != null)
                        .filter(q -> RDFS.Resource.getURI().equals(q.getOrigin().toString())).findFirst();
        if (uriQ.isPresent())
            return uriQ.get();
        throw new IllegalArgumentException("Root question has no subquestion that maps to URI");
    }

    private Set<Question> findRegularQ(Question root) {
        return FormUtils.flatten(root).stream()
                .filter(q -> q.getSubQuestions() == null || q.getSubQuestions().isEmpty())
                .filter(q -> q.getOrigin() != null)
                .filter(q -> !RDFS.Resource.getURI().equals(q.getOrigin().toString()))
                .collect(Collectors.toSet());
    }

    private Optional<Model> getTTLModel(Question root) {
        Optional<Question> ttl = root.getSubQuestions().stream()
                .filter(q -> q.getLayoutClass().contains("TTL"))
                .map(q -> q.getSubQuestions().iterator().next())
                .findFirst();
        return ttl.map(q -> {
            Model m = ModelFactory.createDefaultModel();
            m.read(q.getAnswers().iterator().next().getTextValue());
            return m;
        });
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
        if (a.getTextValue() != null && !a.getTextValue().isEmpty()) {
            return ResourceFactory.createStringLiteral(a.getTextValue());
        }
        return null;
    }

    private Answer getAnswer(RDFNode node) {
        Answer a = new Answer();
        if (node.isURIResource()) {
            a.setCodeValue(URI.create(node.asResource().getURI()));
            a.setHash(DigestUtils.sha1Hex(node.asResource().getURI()));
        } else if (node.isLiteral()) {
            a.setTextValue(node.asLiteral().getString());
            a.setHash(DigestUtils.sha1Hex(node.asLiteral().getLexicalForm()));
        } else if (node.isAnon()) {
            a.setTextValue(AnonNodeTransformer.serialize(node));
            a.setHash(DigestUtils.sha1Hex(AnonNodeTransformer.serialize(node)));
        } else {
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
            return URI.create(SFormsVocabularyJena.s_c_answer_origin.toString() +
                    "/" + createMd5Hash(statement.getObject().toString()));
        return URI.create(SFormsVocabularyJena.s_c_answer_origin.toString() +
                "/" + createMd5Hash(AnonNodeTransformer.serialize(statement.getObject())));
    }

    private String createMd5Hash(String text) {
        return DigestUtils.md5Hex(text);
    }

    private void initializeQuestionUri(Question q) {
        q.setUri(URI.create(SFormsVocabularyJena.s_c_question + "-" + UUID.randomUUID()));
    }

    private Question createQuestion(Resource resource) {
        Question q = new Question();
        initializeQuestionUri(q);
        q.setLabel(resource.getURI());

        StringBuilder descriptionBuilder = new StringBuilder();
        StmtIterator labelIt = resource.listProperties(RDFS.label);
        if (labelIt.hasNext()) {
            descriptionBuilder.append(labelIt.nextStatement().getObject().asLiteral().getString());
            descriptionBuilder.append("\n\n");
        }
        StmtIterator descriptionIt = resource.listProperties(RDFS.comment);
        if (descriptionIt.hasNext())
            descriptionBuilder.append(descriptionIt.nextStatement().getObject().asLiteral().getString());

        q.setDescription(descriptionBuilder.toString());
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
            if (!(o instanceof OriginPair p))
                return false;
            return Objects.equals(q, p.q) && Objects.equals(a, p.a);
        }

        @Override
        public String toString() {
            return "<" + q.toString() + ", " + a.toString() + ">";
        }
    }

    Model extractModel(Statement st) {
        Model model = st.getModel(); // Iterate through subgraphs and find model defining st and return it (or IRI)
        return find(st, model, Optional.empty()).orElse(null);
    }

    private Optional<Model> find(Statement st, Model m, Optional<Model> res) {
        if (res.isPresent())
            return res;
        if (!m.contains(st))
            return Optional.empty();
        if (m instanceof OntModel && ((OntModel) m).listSubModels().toList().stream().anyMatch(sm -> sm.contains(st))) {
            Optional<Optional<Model>> o = ((OntModel) (m)).listSubModels().toList().stream().map(sm -> find(st, sm, res)).filter(Optional::isPresent).findFirst();
            if (o.isPresent())
                return o.get();
        } else
            return Optional.of(m);
        return Optional.empty();
    }
}
