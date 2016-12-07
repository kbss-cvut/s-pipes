package cz.cvut.sempipes.registry;

import java.io.InputStream;

/**
 * Created by Miroslav Blasko on 29.11.16.
 */
public class StringStreamResource implements StreamResource {

    private final String uri;
    private final byte[] content;
    private final String contentType;

    public StringStreamResource(String uri, byte[] content, String contentType) {
        this.uri = uri;
        this.content = content;
        this.contentType = contentType;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public byte[] getContent() {
        return content;
    }

    @Override
    public String getContentType() {
        return contentType;
    }
}
