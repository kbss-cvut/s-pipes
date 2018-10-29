package cz.cvut.sforms.model;

import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.sforms.Vocabulary;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@OWLClass(iri = Vocabulary.s_c_question)
public class Question extends AbstractEntity {

    @OWLDataProperty(iri = Vocabulary.s_p_label)
    private String label;

    @OWLDataProperty(iri = Vocabulary.s_p_description)
    private String description;

    @OWLAnnotationProperty(iri = Vocabulary.s_p_has_question_origin)
    private URI origin;

    @OWLObjectProperty(iri = Vocabulary.s_p_has_related_question, cascade = {CascadeType.MERGE,
            CascadeType.REMOVE}, fetch = FetchType.EAGER)
    private Set<Question> subQuestions = new HashSet<>();

    @OWLObjectProperty(iri = Vocabulary.s_p_has_preceding_question, cascade = {CascadeType.MERGE,
        CascadeType.REMOVE}, fetch = FetchType.EAGER)
    private Set<Question> precedingQuestions = new HashSet<>();

    @OWLObjectProperty(iri = Vocabulary.s_p_has_answer, cascade = {CascadeType.ALL}, fetch = FetchType.EAGER)
    private Set<Answer> answers = new HashSet<>();

    @OWLDataProperty(iri = Vocabulary.s_p_has_layout_class)
    private Set<String> layoutClass = new HashSet<>();

    @OWLDataProperty(iri = Vocabulary.s_p_has_possible_values_query)
    private String possibleValuesQuery;

    // TODO shold not be part of generic question
    @OWLObjectProperty(iri = Vocabulary.s_p_has_declared_prefix, fetch = FetchType.EAGER)
    private Set<PrefixDefinition> declaredPrefix = new HashSet<>();

    public Set<PrefixDefinition> getDeclaredPrefix() {
        return declaredPrefix;
    }

    public void setDeclaredPrefix(Set<PrefixDefinition> declaredPrefix) {
        this.declaredPrefix = declaredPrefix;
    }

    @Types
    private Set<String> types = new HashSet<>();

    @Properties
    private Map<String, Set<String>> properties;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPossibleValuesQuery() {
        return possibleValuesQuery;
    }

    public void setPossibleValuesQuery(String possibleValuesQuery) {
        this.possibleValuesQuery = possibleValuesQuery;
    }

    public Question() {
    }

    public Question(Question other) {
        if (other.subQuestions != null) {
            this.subQuestions = other.subQuestions.stream().map(Question::new).collect(Collectors.toSet());
        }
        if (other.answers != null) {
            this.answers = other.answers.stream().map(Answer::new).collect(Collectors.toSet());
        }
        if (other.types != null) {
            this.types.addAll(other.types);
        }
        this.origin = other.origin;
    }

    public Set<Question> getPrecedingQuestions() {
        return precedingQuestions;
    }

    public void setPrecedingQuestions(Set<Question> precedingQuestions) {
        this.precedingQuestions = precedingQuestions;
    }


    public Set<Question> getSubQuestions() {
        return subQuestions;
    }

    public void setSubQuestions(Set<Question> subQuestions) {
        this.subQuestions = subQuestions;
    }

    public Set<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(Set<Answer> answers) {
        this.answers = answers;
    }

    public URI getOrigin() {
        return origin;
    }

    public void setOrigin(URI origin) {
        this.origin = origin;
    }

    public Set<String> getTypes() {
        return types;
    }

    public void setTypes(Set<String> types) {
        this.types = types;
    }

    public Set<String> getLayoutClass() {
        return layoutClass;
    }

    public void setLayoutClass(Set<String> layoutClass) {
        this.layoutClass = layoutClass;
    }

    public Map<String, Set<String>> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Set<String>> properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "Question (" + label + "){" +
                "answers=" + answers +
                ", subQuestions=" + subQuestions +
                '}';
    }
}
