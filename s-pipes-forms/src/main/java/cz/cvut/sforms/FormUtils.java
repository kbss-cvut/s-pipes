package cz.cvut.sforms;

import cz.cvut.sforms.model.Question;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class FormUtils {

    public static Set<Question> flatten(Set<Question> questions) {
        return questions.stream().flatMap((q) -> flatten(q).stream()).collect(Collectors.toSet());
    }

    public static Set<Question> flatten(Question root) {
        if (root == null) {
            return Collections.emptySet();
        }
        return root.getSubQuestions().stream().flatMap((q) -> q.getSubQuestions().stream()).collect(Collectors.toSet());
    }
}
