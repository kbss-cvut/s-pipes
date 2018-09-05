package cz.cvut.sforms.util;

import cz.cvut.sforms.model.Question;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class FormUtils {

    public static String SFORMS_MODEL_PACKAGE_NAME = Question.class.getPackage().getName();

    public static Set<Question> flatten(Set<Question> questions) {
        return questions.stream().flatMap((q) -> flatten(q).stream()).collect(Collectors.toSet());
    }

    /**
     * Returns all questions within a tree rooted by <code>root<code/> question.
     * The <code>root</code> question is included as well.
     *
     * @param root question representing root of the question tree.
     * @return Set of question.
     */
    public static Set<Question> flatten(Question root) {
        if (root == null) {
            return Collections.emptySet();
        }
        Set<Question> qSet = root.getSubQuestions().stream().flatMap((q) -> flatten(q).stream()).collect(Collectors.toSet());
        qSet.add(root);
        return qSet;
    }
}
