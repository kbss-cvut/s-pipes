package cz.cvut.spipes.recursion;

import java.util.stream.Stream;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CombinedQueryTemplateRecursionProviderTest {

    final Model emptyModel = ModelFactory.createDefaultModel();
    @Mock
    QueryTemplateRecursionProvider parentProvider = null;
    @Mock
    QueryTemplateRecursionProvider childProvider = null;
    CombinedQueryTemplateRecursionProvider combinedProvider;

    @BeforeEach
    public void initTest() {
        combinedProvider = new CombinedQueryTemplateRecursionProvider(-1, parentProvider, childProvider);
    }


    @Test
    public void shouldTerminateFailsIfFirstStepIsDifferentFromZero() {
        assertThrows(
            IllegalStateException.class,
            () -> callShouldTerminate(combinedProvider, 3)
        );
    }

    @Test
    public void shouldTerminateFailsIfCalledInNonlinearOrder() {
        assertThrows(
            IllegalStateException.class,
            () -> callShouldTerminate(combinedProvider, 0, 1, 3)
        );
    }

    @Test
    public void shouldTerminateSucceedsIfCalledInLinearOrder() {
        callShouldTerminate(combinedProvider, 0, 1, 2);
    }

    @Test
    public void shouldTerminateCallsSubProvidersMethods() {


        doReturn(Boolean.FALSE).when(parentProvider).shouldTerminate(0, emptyModel, emptyModel);
        doReturn(Boolean.FALSE).when(parentProvider).shouldTerminate(1, emptyModel, emptyModel);
        doReturn(Boolean.FALSE).when(parentProvider).shouldTerminate(2, emptyModel, emptyModel);
        doReturn(Boolean.TRUE).when(parentProvider).shouldTerminate(3, emptyModel, emptyModel);

        doReturn(Boolean.FALSE).when(childProvider).shouldTerminate(0, emptyModel, emptyModel);
        doReturn(Boolean.FALSE).when(childProvider).shouldTerminate(1, emptyModel, emptyModel);
        doReturn(Boolean.FALSE).when(childProvider).shouldTerminate(2, emptyModel, emptyModel);
        doReturn(Boolean.FALSE).when(childProvider).shouldTerminate(3, emptyModel, emptyModel);
        doReturn(Boolean.TRUE).when(childProvider).shouldTerminate(4, emptyModel, emptyModel);

        callShouldTerminateUntilItReturnsTrue(combinedProvider);

        verify(parentProvider, times(1)).shouldTerminate(0, emptyModel, emptyModel);
        verify(childProvider, times(1)).shouldTerminate(0, emptyModel, emptyModel);

        verify(parentProvider, times(1)).shouldTerminate(1, emptyModel, emptyModel);
        verify(parentProvider, times(1)).shouldTerminate(2, emptyModel, emptyModel);
        verify(parentProvider, times(1)).shouldTerminate(3, emptyModel, emptyModel);

        verify(parentProvider, times(0)).shouldTerminate(4, emptyModel, emptyModel);


        verify(childProvider, times(2)).shouldTerminate(1, emptyModel, emptyModel);
        verify(childProvider, times(2)).shouldTerminate(2, emptyModel, emptyModel);
        verify(childProvider, times(2)).shouldTerminate(3, emptyModel, emptyModel);
        verify(childProvider, times(2)).shouldTerminate(4, emptyModel, emptyModel);

        verify(childProvider, times(0)).shouldTerminate(5, emptyModel, emptyModel);

    }

    @Test
    public void shouldTerminateReturnsTrueIfParentProviderReturnsTrueInZeroIteration() {
        when(parentProvider.shouldTerminate(0, emptyModel, emptyModel)).thenReturn(Boolean.TRUE);
        boolean shouldTerminate = combinedProvider.shouldTerminate(
            0,
            emptyModel,
            emptyModel
        );
        verify(parentProvider, times(1)).shouldTerminate(0, emptyModel, emptyModel);
        verify(childProvider, times(0)).shouldTerminate(0, emptyModel, emptyModel);
        assertTrue(shouldTerminate);
    }

    @Test
    public void shouldTerminateReturnsTrueIfChildProviderReturnsTrueInZeroIteration() {
        when(childProvider.shouldTerminate(0, emptyModel, emptyModel)).thenReturn(Boolean.TRUE);
        boolean shouldTerminate = combinedProvider.shouldTerminate(
            0,
            emptyModel,
            emptyModel
        );
        verify(parentProvider, times(1)).shouldTerminate(0, emptyModel, emptyModel);
        verify(childProvider, times(1)).shouldTerminate(0, emptyModel, emptyModel);
        assertTrue(shouldTerminate);
    }


    private void callShouldTerminate(CombinedQueryTemplateRecursionProvider provider, Integer... currentIteration) {
        Stream.of(currentIteration).forEach(
            ci -> provider.shouldTerminate(ci, emptyModel, emptyModel)
        );
    }

    private void callShouldTerminateUntilItReturnsTrue(CombinedQueryTemplateRecursionProvider provider) {

        for (int i = 0; i < 100; i++) {
            if (provider.shouldTerminate(i, emptyModel, emptyModel)) {
                return;
            }
        }
        fail("Method shouldTerminate() did not return true which would caused infinite cycle.");
    }
}