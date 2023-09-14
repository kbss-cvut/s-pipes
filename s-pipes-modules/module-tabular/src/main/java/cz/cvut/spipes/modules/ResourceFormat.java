package cz.cvut.spipes.modules;

import cz.cvut.spipes.exception.ResourceNotFoundException;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum ResourceFormat {
    PLAIN("text/plain"),
    CSV("text/csv"),
    TSV("text/tab-separated-values"),
    HTML("text/html"),
    EXCEL("application/vnd.ms-excel");
    private final String localName;

    public Resource getResource() {
        return ResourceFactory.createResource(this.localName);
    }

    public static ResourceFormat fromResource(Resource resource) {
        return Arrays.stream(ResourceFormat.values())
                .filter(d -> d.getResource().equals(resource))
                .findAny().orElseThrow(() -> new ResourceNotFoundException(
                        "Resource " + resource + " not recognized among valid values of this type, i.e. " +
                                Arrays.stream(Mode.values()).map(Mode::getResource).collect(Collectors.toList())
                ));
    }

    ResourceFormat(String localName) {
        this.localName = localName;
    }
}
