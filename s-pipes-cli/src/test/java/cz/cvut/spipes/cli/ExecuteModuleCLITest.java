package cz.cvut.spipes.cli;

import java.io.IOException;
import org.junit.jupiter.api.Test;

public class ExecuteModuleCLITest {

    @Test
    public void main() throws Exception {
    }

    public void mainSucceedWithOnlyExecutionTarget() {
        // bind -> world --> hello world
    }

    public void mainParametersAreSetToInputBinding() {

    }

    public void mainInputGraphIsSetFromStdIn() {

    }

    public void mainInputGraphIsSetByParameters() throws IOException {
        ExecuteModuleCLI.main(new String[]{
            "--input-data-from-stdin"
            }
        );

    }

    // load java -> rdf mapping of semantic modules
    // load definitions of semantic modules
    // load configuration of semantic modules

}