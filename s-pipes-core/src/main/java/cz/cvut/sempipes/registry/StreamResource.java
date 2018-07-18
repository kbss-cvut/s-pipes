package cz.cvut.sempipes.registry;

public interface StreamResource {

    String getUri();

    byte[] getContent();

    String getContentType();

}
