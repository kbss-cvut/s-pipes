package cz.cvut.spipes.spin.model;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public interface SPINResource extends Resource {
    String getComment();

    String getString(Property predicate);
}
