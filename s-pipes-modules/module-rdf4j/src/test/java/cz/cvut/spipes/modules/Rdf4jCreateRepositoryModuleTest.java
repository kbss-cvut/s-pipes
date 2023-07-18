package cz.cvut.spipes.modules;

import cz.cvut.spipes.exceptions.RepositoryAlreadyExistsException;
import org.eclipse.rdf4j.repository.manager.RepositoryManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class Rdf4jCreateRepositoryModuleTest {

    @Mock
    private RepositoryManager repositoryManager;

    @Test
    void executeSelfFailsIfRdf4jIgnoreIfExistsIsFalseAndRepositoryExists() {
        Rdf4jCreateRepositoryModule rdf4CreateRepositoryModule = new Rdf4jCreateRepositoryModule();
        rdf4CreateRepositoryModule.setRdf4jIgnoreIfExists(false);
        rdf4CreateRepositoryModule.setRepositoryManager(repositoryManager);


        given(repositoryManager.hasRepositoryConfig(any())).willReturn(true);
        assertThrows(RepositoryAlreadyExistsException.class, rdf4CreateRepositoryModule::executeSelf);
        verify(repositoryManager, times(1)).init();
    }

}