package cz.cvut.spipes.modules;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class AbstractModuleTest extends AbstractCoreModuleTestHelper {

    @Override
    String getModuleName() {
        return "abstract";
    }

    @Disabled
    @Test
    public void throwValidationExceptionIfValidationConstrainFailsAndExitOnErrorIsTrue() {
//        AbstractModule m = createModuleWithFailingValidationConstraint();
//
//        m.setInputContext(ExecutionContextFactory.createEmptyContext());
//        m.checkInputConstraints();
    }

    @Disabled
    @Test
    public void throwNoValidationExceptionIfValidationConstrainFailsAndExitOnErrorIsFalse() {

    }


}