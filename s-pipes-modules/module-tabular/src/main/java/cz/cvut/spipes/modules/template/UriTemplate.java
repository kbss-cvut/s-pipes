package cz.cvut.spipes.modules.template;

import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;

import java.util.List;

public class UriTemplate {

    private final StringTemplate template;

    public UriTemplate(String templateAsString) {
        this.template = new StringTemplate(templateAsString);
    }

    public void initialize(String tableUri, List<String> header) throws InvalidTemplateException {
        template.initialize(tableUri, header);
    }

    public IRI getUri(List<String> row) {
        String val = template.process(row);

        if (val != null) {
            return IRIFactory.iriImplementation().create(val);
        }

        return null;
    }
}
