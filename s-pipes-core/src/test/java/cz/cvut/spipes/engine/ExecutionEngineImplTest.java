package cz.cvut.spipes.engine;

import cz.cvut.spipes.modules.BindWithConstantModule;
import java.util.Arrays;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.BDDMockito.given;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Created by Miroslav Blasko on 31.5.16.
 */
@RunWith(MockitoJUnitRunner.class)
public class ExecutionEngineImplTest {

    @Mock
    BindWithConstantModule inputModule1;
    @Mock
    BindWithConstantModule inputModule2;
    @Mock
    BindWithConstantModule outputModule;


    @Ignore
    @Test
    //TODO refactor configuration resource should not be required !?, only one of execute and getOutputContext should be mocked
    public void executePipelineExecutesDependencies() {

        given(inputModule1.execute()).willReturn(ExecutionContextFactory.createEmptyContext());
        given(inputModule1.getOutputContext()).willReturn(ExecutionContextFactory.createEmptyContext());
        given(inputModule1.getResource()).willReturn(
            ResourceFactory.createResource("http://example.org/input-module-1")
        );

        given(inputModule2.execute()).willReturn(ExecutionContextFactory.createEmptyContext());
        given(inputModule2.getOutputContext()).willReturn(ExecutionContextFactory.createEmptyContext());
        given(inputModule2.getResource()).willReturn(
            ResourceFactory.createResource("http://example.org/input-module-2")
        );


        given(outputModule.getResource()).willReturn(
            ResourceFactory.createResource("http://example.org/output-module")
        );
        given(outputModule.getInputModules()).willReturn(
            Arrays.asList(inputModule1, inputModule2)
        );
        given(outputModule.getOutputContext()).willReturn(ExecutionContextFactory.createEmptyContext());

        ExecutionEngine engine = new ExecutionEngineImpl();
        ExecutionContext outputContext = engine.executePipeline(outputModule, ExecutionContextFactory.createEmptyContext());

        verify(inputModule1, times(1)).execute();
        verify(inputModule2, times(1)).execute();
        verify(outputModule, times(1)).execute();
    }


}