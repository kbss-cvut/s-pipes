package cz.cvut.spipes.modules;

import cz.cvut.spipes.exception.ResourceNotFoundException;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Mode {
    MINIMAL("minimal-mode"),
    STANDARD("standard-mode");

    private static final String PREFIX = TabularModule.TYPE_URI + "/";
    private final String localName;

    public Resource getResource() {
        return ResourceFactory.createResource(PREFIX + this.localName);
    }

    public static Mode fromResource(Resource resource) {
        return Arrays.stream(Mode.values())
            .filter(d -> d.getResource().equals(resource))
            .findAny().orElseThrow(() -> new ResourceNotFoundException(
                "Resource " + resource + " not recognized among valid values of this type, i.e. " +
                    Arrays.stream(Mode.values()).map(Mode::getResource).collect(Collectors.toList())
            ));
    }

    Mode(String localName) {
        this.localName = localName;
    }
}
