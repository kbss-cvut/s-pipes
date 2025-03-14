package cz.cvut.sforms;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class SFormsVocabularyJena {

    private SFormsVocabularyJena() {
        throw new IllegalStateException("Utility class");
    }

    public static final Property s_p_constraint = getProperty(cz.cvut.sforms.Vocabulary.s_p_spl_constraint);
    public static final Property s_p_defaultValue = getProperty(cz.cvut.sforms.Vocabulary.s_p_spl_defaultValue);
    public static final Property s_p_optional = getProperty(cz.cvut.sforms.Vocabulary.s_p_spl_optional);
    public static final Property s_p_predicate_A = getProperty(Vocabulary.s_p_spl_predicate);
    public static final Property s_p_valueType = getProperty(cz.cvut.sforms.Vocabulary.s_p_spl_valueType);
    public static final Property s_p_minCount = getProperty(cz.cvut.sforms.Vocabulary.s_p_spl_minCount);
    public static final Property s_p_maxCount = getProperty(cz.cvut.sforms.Vocabulary.s_p_spl_maxCount);
    public static final Property s_p_has_related_question = getProperty(cz.cvut.sforms.Vocabulary.s_p_has_related_question);
    public static final Property s_p_has_answer = getProperty(cz.cvut.sforms.Vocabulary.s_p_has_answer);
    public static final Property s_p_has_origin_path = getProperty(cz.cvut.sforms.Vocabulary.s_p_has_origin_path);
    public static final Property s_p_has_origin_path_id = getProperty(cz.cvut.sforms.Vocabulary.s_p_has_origin_path_id);
    public static final Property s_p_has_question_origin = getProperty(cz.cvut.sforms.Vocabulary.s_p_has_question_origin);
    public static final Property s_p_has_answer_origin = getProperty(cz.cvut.sforms.Vocabulary.s_p_has_answer_origin);
    public static final Property s_p_has_possible_values_query = getProperty(Vocabulary.s_p_has_possible_values_query);
    public static final Property s_p_has_possible_value = getProperty(Vocabulary.s_p_has_possible_value);
    public static final Property s_p_textual_view = getProperty(Vocabulary.s_p_textual_view);

    public static final Resource s_c_question = getProperty(cz.cvut.sforms.Vocabulary.s_c_question);
    public static final Resource s_c_answer = getProperty(cz.cvut.sforms.Vocabulary.s_c_answer);
    public static final Resource s_c_question_origin = getProperty(Vocabulary.s_c_question_origin);
    public static final Resource s_c_answer_origin = getProperty(Vocabulary.s_c_answer_origin);


    static Property getProperty(String url) {
        return ResourceFactory.createProperty(url);
    }

    static Resource getResource(String url) {
        return ResourceFactory.createResource(url);
    }

    static Property getProperty(String prefix, String localName) {
        return ResourceFactory.createProperty(prefix + localName);
    }

    static Resource getResource(String prefix, String localName) {
        return ResourceFactory.createResource(prefix + localName);
    }
}
