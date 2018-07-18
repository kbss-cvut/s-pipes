package cz.cvut.spipes.rest;

/**
 * Created by Miroslav Blasko on 28.11.16.
 */
class StreamResourceDTO {
    String id;
    String persistentUri;
    String alternativeUri;

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
}
