package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ListHandler extends Handler<List<?>> {

    private final Map<Class<?>, Function<Object, ?>> handlerMap = new HashMap<>();

    public ListHandler(Resource resource, ExecutionContext executionContext, Setter<? super List<?>> setter) {
        super(resource, executionContext, setter);
    }

    @Override
    public void setValueByProperty(Property property) {
        setter.addValue(getRDFNodeListByProperty(property));
    }

    public List<RDFNode> getRDFNodeListByProperty(Object property) {
        return resource.listProperties((Property) property)
                .toList().stream()
                .map(st -> st.getObject().asResource())
                .collect(Collectors.toList());
    }

}
