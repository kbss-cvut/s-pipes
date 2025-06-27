package cz.cvut.spipes.spin.model;

import org.apache.jena.rdf.model.Resource;

public class SPINFactory {
    public static Query asQuery(Resource resource){
        if(resource.canAs(cz.cvut.spipes.spin.model.Select.class)) {
            return resource.as(Select.class);
        }
        else if(resource.canAs(cz.cvut.spipes.spin.model.Construct.class)) {
            return resource.as(Construct.class);
        }
        else if(resource.canAs(cz.cvut.spipes.spin.model.Ask.class)) {
            return resource.as(Ask.class);
        }
        else if(resource.canAs(cz.cvut.spipes.spin.model.Describe.class)) {
            return resource.as(Describe.class);
        }
        else {
            return null;
        }
    }
}
