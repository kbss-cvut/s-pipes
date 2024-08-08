package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import java.nio.file.Path;
import java.nio.file.Paths;


public class PathHandler extends Handler<Path> {
    public PathHandler(Resource resource, ExecutionContext executionContext, Setter<? super Path> setter) {
        super(resource, executionContext, setter);
    }

    @Override
    public void setValueByProperty(Property property) {
        RDFNode pathNode = getEffectiveValue(property);
        if(pathNode != null) {
            Path path = Paths.get(pathNode.toString());
            setter.addValue(path);
        }
    }
}
