package cz.cvut.sempipes;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Created by Miroslav Blasko on 6.6.17.
 */
public class VocabularyJena {

    public static Property s_p_constraint = getProperty(Vocabulary.s_p_constraint);
    public static Property s_p_defaultValue = getProperty(Vocabulary.s_p_defaultValue);
    public static Property s_p_optional = getProperty(Vocabulary.s_p_optional);
    public static Property s_p_predicate = getProperty(Vocabulary.s_p_predicate);
    public static Property s_p_valueType = getProperty(Vocabulary.s_p_valueType);
    public static Property s_p_minCount = getProperty(Vocabulary.s_p_minCount);
    public static Property s_p_maxCount = getProperty(Vocabulary.s_p_maxCount);
    public static Property s_p_has_related_question = getProperty(Vocabulary.s_p_has_related_question);
    public static Property s_p_has_answer = getProperty(Vocabulary.s_p_has_answer);
    public static Property s_p_has_origin_path = getProperty(Vocabulary.s_p_has_origin_path);
    public static Property s_p_has_origin_path_id = getProperty(Vocabulary.s_p_has_origin_path_id);
    public static Property s_p_has_question_origin = getProperty(Vocabulary.s_p_has_question_origin);
    public static Property s_p_has_answer_origin = getProperty(Vocabulary.s_p_has_answer_origin);

    public static Resource s_c_question = getProperty(Vocabulary.s_c_question);
    public static Resource s_c_answer = getProperty(Vocabulary.s_c_answer);
    public static Resource s_c_question_origin = getProperty(Vocabulary.s_c_question_origin);


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
