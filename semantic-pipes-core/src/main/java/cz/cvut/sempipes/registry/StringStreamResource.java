package cz.cvut.sempipes.registry;

/**
 * Created by Miroslav Blasko on 29.11.16.
 */
public class StringStreamResource implements StreamResource {

    private final String uri;
    private final String content;

    public StringStreamResource(String uri, String content) {
        this.uri = uri;
        this.content = content;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public String getContentAsString() {
        return content;
    }
}
