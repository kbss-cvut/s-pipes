package cz.cvut.sforms.util;

import cz.cvut.sforms.model.Question;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FormUtils {

    public static String SFORMS_MODEL_PACKAGE_NAME = Question.class.getPackage().getName();

    /**
     * Returns all questions within a tree rooted by provided <code>rootQuestions<code/>.
     * The <code>rootQuestions</code> question is included as well.
     *
     * @param rootQuestions questions representing root of the question tree.
     * @return Set of question.
     */
    public static Set<Question> flatten(Set<Question> rootQuestions) {
        return rootQuestions.stream().flatMap((q) -> flatten(q).stream()).collect(Collectors.toSet());
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

    @NotNull
    public static Stream<Question> flatten(Stream<Question> roots) {
        return roots.flatMap(FormUtils::flattenQuestion);
    }

    private static Stream<Question> flattenQuestion(Question root) {
        return Stream.concat(
            Stream.of(root),
            root.getSubQuestions().stream().flatMap(FormUtils::flattenQuestion)
        );
    }

    public static long getQuestionsSize(Question root) {
        return flatten(root).stream().count();
    }

    public static long getQuestionLeavesSize(Question root) {
        return flatten(root).stream().filter(q -> q.getSubQuestions().isEmpty()).count();
    }
}
