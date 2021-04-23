package cz.cvut.spipes.form;

import cz.cvut.spipes.VocabularyJena;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;

public class JenaFormUtils {

    public static ExtendedIterator<Resource> getQuestions(Model formModel) {
        return formModel.listSubjects()
            .filterKeep(
                subj -> subj.hasProperty(
                    RDF.type,
                    VocabularyJena.s_c_question
                )
            );
    }

    public static Resource getQuestionOrigin(Resource formEntity) {
        return formEntity.getPropertyResourceValue(VocabularyJena.s_p_has_question_origin);
    }

    public static Resource getAnswerOrigin(Resource formEntity) {
        return formEntity.getPropertyResourceValue(VocabularyJena.s_p_has_answer_origin);
    }
}
