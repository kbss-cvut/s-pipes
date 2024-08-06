package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

public class StringHandler extends Handler<String> {


    public StringHandler(Resource resource, ExecutionContext executionContext, Setter<? super String> setter) {
        super(resource, executionContext, setter);
    }

    @Override
    public void setValueByProperty(Property property) {

        Statement s = resource.getProperty(property);

        if (s != null && s.getObject().isLiteral()) {
            setter.addValue(s.getString());
        }
    }
}
