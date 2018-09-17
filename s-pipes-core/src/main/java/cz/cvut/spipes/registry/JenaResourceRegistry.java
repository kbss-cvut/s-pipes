package cz.cvut.spipes.registry;


import cz.cvut.spipes.exception.ResourceNotFoundException;
import cz.cvut.spipes.exception.ResourceNotUniqueException;
import cz.cvut.spipes.util.JenaUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.jetbrains.annotations.NotNull;

/**
 * TODO id can be defined by some property related to the resource ?
 * <p>
 */
public class JenaResourceRegistry implements  ResourceRegistry {

    Map<String, Set<String>> local2fullNamesMap = new HashMap<>();
    Map<String, Set<String>> fullName2ContextsMap = new HashMap<>();
    Map<String, Map<String, String>> context2PrefixMappingMap = new HashMap<>();

    public JenaResourceRegistry(List<Resource> resourceList) {

        resourceList.stream()
                .forEach(res -> {
                    String resUri = res.getURI();
                    String localName = res.getLocalName();
                    Map<String, String> prefixMap = res.getModel().getNsPrefixMap();

                    String contextUri = JenaUtils.getBaseUri(ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, res.getModel()).getBaseModel());

                    local2fullNamesMap
                            .compute(localName, (k, v) -> (v == null) ? new HashSet<>() : v)
                            .add(resUri);

                    fullName2ContextsMap
                            .compute(resUri, (k, v) -> (v == null) ? new HashSet<>() : v)
                            .add(contextUri);

                    context2PrefixMappingMap
                            .putIfAbsent(contextUri, new HashMap<>(prefixMap));
                });
    }


    @Override
    public @NotNull Set<String> getAllContexts() {
        return Collections.unmodifiableSet(context2PrefixMappingMap.keySet());
    }

    @Override
    public Set<String> getContexts(String entityId) {

        if (isFullNameEntityId(entityId)) {
            return Collections.unmodifiableSet(fullName2ContextsMap.get(entityId));
        }

        if (isLocalNameEntityId(entityId)) {
            return getLocalName2Contexts(entityId);
        }

        if (isPrefixedEntityId(entityId)) {
            String prefix = getPrefix(entityId);
            String localName = getLocalName(entityId);
            throw new UnsupportedOperationException(); //TODO
        }

        throw new IllegalStateException();
    }

    @Override
    public @NotNull String getResourceUri(@NotNull String entityId,
                                 @NotNull String contextUri) throws ResourceNotFoundException, ResourceNotUniqueException{
        if (isFullNameEntityId(entityId)) {
            if (fullName2ContextsMap.get(entityId).contains(contextUri)) {
                return entityId;
            }
            throw new ResourceNotFoundException(entityId, contextUri);
        }


        if (isLocalNameEntityId(entityId)) {
            Set<String> fullIds = local2fullNamesMap.get(entityId).stream()
                    .filter(fullId -> fullName2ContextsMap.get(fullId).contains(contextUri))
                    .collect(Collectors.toSet());
            if (fullIds.isEmpty()) {
                throw new ResourceNotFoundException(entityId, contextUri);
            }

            if (fullIds.size() > 1) {
                throw new ResourceNotUniqueException(entityId, fullIds, contextUri);
            }

            return fullIds.iterator().next();
        }

        if (isPrefixedEntityId(entityId)) {
            throw new UnsupportedOperationException();
        }

        throw new IllegalStateException();
    }

    @Override
    public @NotNull String getResourceUri(@NotNull String entityId) {
        Set<String> contexts = getContexts(entityId);

        if (contexts.isEmpty()) {
            throw new ResourceNotFoundException(entityId, getAllContexts());
        }
        if (contexts.size() > 2) {
            throw new ResourceNotUniqueException("Resource not unique"); //TODO better message
        }

        return getResourceUri(entityId, contexts.iterator().next());
    }



    private Set<String> getLocalName2Contexts(String localEntityId) {
        return local2fullNamesMap.getOrDefault(localEntityId, new HashSet<>()).stream()
                .flatMap(fullId -> fullName2ContextsMap.get(fullId).stream())
                .collect(Collectors.toSet());
    }

    private String getPrefix(String prefixedEntityId) {
        return prefixedEntityId.substring(0, prefixedEntityId.indexOf(':'));
    }

    private String getLocalName(String prefixedEntityId) {
        return prefixedEntityId.substring(prefixedEntityId.indexOf(':') + 1);
    }


    private boolean isPrefixedEntityId(String entityId) {
        return entityId.contains(":");
    }

    private boolean isLocalNameEntityId(String entityId) {
        return !(isPrefixedEntityId(entityId) || isFullNameEntityId(entityId));
    }

    private boolean isFullNameEntityId(String entityId) {
        return entityId.contains("#") || entityId.contains("/");
    }


}
