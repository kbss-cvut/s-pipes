package cz.cvut.spipes.registry;

import cz.cvut.spipes.exception.ResourceNotFoundException;
import cz.cvut.spipes.exception.ResourceNotUniqueException;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

/**
 *
 * Registers resources and its alternative ids with respect to contexts in which they are defined.
 *
 *
 * Created by Miroslav Blasko on 30.7.16.
 */
public interface ResourceRegistry {


    @NotNull Set<String> getAllContexts();

    @NotNull Set<String> getContexts(String entityId);

    @NotNull String getResourceUri(@NotNull  String entityId, @NotNull  String contextUri) throws ResourceNotFoundException, ResourceNotUniqueException;


    @NotNull String getResourceUri(@NotNull String entityId) throws ResourceNotFoundException, ResourceNotUniqueException;
}
