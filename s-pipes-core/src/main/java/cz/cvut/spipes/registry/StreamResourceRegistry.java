package cz.cvut.spipes.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.*;

public class StreamResourceRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(StreamResourceRegistry.class);

    private static StreamResourceRegistry instance;
    private Set<String> resourcePrefixMap = new HashSet<>();
    private static final String PERSISTENT_CONTEXT_PREFIX = "http://onto.fel.cvut.cz/resources/";
    private Map<String, WeakReference<StreamResource>> id2resourcesMap = new HashMap<>();

    private StreamResourceRegistry() {
    }

    public static StreamResourceRegistry getInstance() {
        if (instance == null) {
            instance = new StreamResourceRegistry();
            instance.resourcePrefixMap.add(PERSISTENT_CONTEXT_PREFIX);
            return instance;
        }
        return instance;
    }

    public void registerResourcePrefix(String resourcePrefix) {
        resourcePrefixMap.add(resourcePrefix);
    }

    public String getPERSISTENT_CONTEXT_PREFIX() {
        return PERSISTENT_CONTEXT_PREFIX;
    }

    public StreamResource getResourceById(String id) {
        return id2resourcesMap.get(id).get();
    }

    public StreamResource getResourceByUrl(String url) {
        LOG.debug("Trying to find resource with url {}", url);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Resource map content: {}", id2resourcesMap);
        }
        String id = resourcePrefixMap.stream()
                .filter(url::startsWith)
                .findAny().map(p -> url.substring(p.length()))
                .orElse(null);
        LOG.debug("- found {}", id);
        StreamResource res = id2resourcesMap.get(id).get();
        if (res == null) {
            return null;
        }
        return new StringStreamResource(url, res.getContent(), res.getContentType()); //TODO remove
    }

    public StreamResource registerResource(String id, byte[] content, String contentType) {
        LOG.debug("Registering resource with id {}", id);
        StreamResource res = new StringStreamResource(id, content, contentType);
        id2resourcesMap.put(id, new WeakReference<>(res));
        if (LOG.isTraceEnabled()) {
            LOG.trace("Resource map content after the registration: {}", id2resourcesMap);
        }
        return res;
    }
}
