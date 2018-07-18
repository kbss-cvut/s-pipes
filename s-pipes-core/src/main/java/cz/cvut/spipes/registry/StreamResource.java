package cz.cvut.spipes.registry;

public interface StreamResource {

    String getUri();

    byte[] getContent();

    String getContentType();

}
