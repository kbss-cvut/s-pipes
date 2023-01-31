package cz.cvut.spipes.rest;

import cz.cvut.spipes.registry.StreamResource;

public class StreamResourceDTO {
    String id;
    String persistentUri;
    String alternativeUri;
    StreamResource resource;

    public StreamResourceDTO(String id,
                             String persistentUriPrefix,
                             String alternativeUriPrefix) {
        this.id = id;
        persistentUri = persistentUriPrefix + id;
        alternativeUri = alternativeUriPrefix + id;
    }

    public String getId() {
        return id;
    }

    public String getPersistentUri() {
        return persistentUri;
    }

    public String getAlternativeUri() {
        return alternativeUri;
    }

    public void attachStreamResource(StreamResource resource) {
        this.resource = resource;
    }
}
