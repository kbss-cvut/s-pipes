package cz.cvut.spipes.modules.template;

import java.util.List;

public class UriTemplate {

    private final StringTemplate template;

    public UriTemplate(String templateAsString) {
        this.template = new StringTemplate(templateAsString);
    }

    public void initialize(String tableUri, List<String> header) throws InvalidTemplateException {
        template.initialize(tableUri, header);
    }
}
