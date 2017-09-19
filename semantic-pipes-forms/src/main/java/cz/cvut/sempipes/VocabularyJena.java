package cz.cvut.sempipes;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Created by Miroslav Blasko on 6.6.17.
 */
public class VocabularyJena {

    public static Property s_p_has_related_question = getProperty(Vocabulary.s_p_has_related_question);
    public static Property s_p_has_answer = getProperty(Vocabulary.s_p_has_answer);
    public static Property s_p_has_origin_path = getProperty(Vocabulary.s_p_has_origin_path);
    public static Property s_p_has_origin_path_id = getProperty(Vocabulary.s_p_has_origin_path_id);
    public static Property s_p_has_question_origin = getProperty(Vocabulary.s_p_has_question_origin);
    public static Property s_p_has_answer_origin = getProperty(Vocabulary.s_p_has_answer_origin);
    public static Resource s_c_question = getProperty(Vocabulary.s_c_question);
    public static Resource s_c_answer = getProperty(Vocabulary.s_c_answer);


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
