package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.util.SPINExpressions;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class PathHandler extends Handler<Path> {
    public PathHandler(Resource resource, ExecutionContext executionContext, Setter<? super Path> setter) {
        super(resource, executionContext, setter);
    }

    @Override
    public void setValueByProperty(Property property) {
        RDFNode pathNode = getRDFNodeByProperty(property);
        if(pathNode != null) {
            Path path = Paths.get(pathNode.asLiteral().toString());
            setter.addValue(path);
        }
    }
}
