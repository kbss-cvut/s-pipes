package cz.cvut.spipes.util;

import cz.cvut.spipes.engine.VariablesBinding;
import java.util.Arrays;

public class VariableBindingUtils {

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
}
