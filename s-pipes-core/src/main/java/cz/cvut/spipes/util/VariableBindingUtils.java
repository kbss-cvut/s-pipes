package cz.cvut.spipes.util;

import cz.cvut.spipes.engine.VariablesBinding;
import org.apache.jena.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

import static org.reflections.Reflections.log;

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

    public static void extendBindingFromURL(VariablesBinding inputVariablesBinding, URL inputBindingURL) {
        try {
            final VariablesBinding vb2 = new VariablesBinding();
            String path = inputBindingURL.getPath();
            File file = new File(path);
            InputStream is = new FileInputStream(file);
            vb2.load(is, FileUtils.langTurtle);
            is.close();
            //vb2.load(inputBindingURL.openStream(), FileUtils.langTurtle);
            VariablesBinding vb3 = inputVariablesBinding.extendConsistently(vb2);
            if (vb3.isEmpty()) {
                log.debug("No conflict found between bindings loaded from '{}' and those provided in query string.",
                        "_pInputBindingURL"
                );
            } else {
                log.warn("Conflicts found between bindings loaded from '{}' and those provided in query string: {}",
                        "_pInputBindingURL", vb3
                );
            }
        } catch (IOException e) {
            log.warn("Could not read data from parameter {}={}, caused by: {}", "_pInputBindingURL", inputBindingURL, e);
        }

    }
}
