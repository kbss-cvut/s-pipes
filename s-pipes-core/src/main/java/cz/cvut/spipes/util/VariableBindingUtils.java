package cz.cvut.spipes.util;

import cz.cvut.spipes.engine.VariablesBinding;
import org.apache.jena.util.FileUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import static org.reflections.Reflections.log;

public class VariableBindingUtils {

    /**
     * Returns new variables binding from provided variables binding restricted to listed variables.
     * @param variablesBinding Variables binding from which values are copied.
     * @param varNames Names of variables that should be copied to the new binding.
     * @throws IllegalStateException If the provided variables binding does not contain a variable with the given name.
     * @return new variables binding
     */
    public static VariablesBinding restrict(VariablesBinding variablesBinding, String... varNames) {
        VariablesBinding newVB = variablesBinding.restrictTo(varNames);
        Arrays.stream(varNames).forEach(
            vn ->  {if (newVB.getNode(vn) == null) {
                throw new IllegalStateException(
                    String.format("Variable binding %s does not contain variable with name %s.\n", variablesBinding, vn)
                );
            } }
        );
        return newVB;
    }

    /**
     * Extends the input variables binding with the variables binding loaded from the provided URL.
     * If the bindings contain conflicting values for the same variable, a warning is logged.
     *
     * @param targetVariablesBinding Binding being extended.
     * @param extendingVariablesBindingURL Url from which binding is loaded and used to extend the target binding.
     * @throws IOException If the binding cannot be loaded from the provided URL.
     */
    public static void extendBindingFromURL(VariablesBinding targetVariablesBinding, URL extendingVariablesBindingURL) throws IOException {
        VariablesBinding vb2 = new VariablesBinding();

        vb2.load(extendingVariablesBindingURL.openStream(), FileUtils.langTurtle);

        VariablesBinding conflictingBindingsOfVb2 = targetVariablesBinding.extendConsistently(vb2);
        if (conflictingBindingsOfVb2.isEmpty()) {
            log.debug("No conflict found when extending variable bindings from '{}'.", extendingVariablesBindingURL);
        } else {
            log.warn("Conflicts found when extending binding '{}' with binding loaded from '{}'. " +
                "Following parts for the loaded binding was not possible to extend '{}'",
                targetVariablesBinding,
                extendingVariablesBindingURL,
                conflictingBindingsOfVb2
            );
        }
    }
}
