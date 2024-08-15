package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.Property;
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
        registerHandlers();
    }

    private void registerHandlers() {
        handlerMap.put(Resource.class, this::getResourcesByProperty);
    }


    Function<Object, ?> getHandler() {
        Type genericType = setter.getField().getGenericType();
        if (genericType instanceof ParameterizedType parameterizedType) {
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (typeArguments.length > 0) {
                Type typeArgument = typeArguments[0];
                if (typeArgument instanceof Class<?>) {
                    return handlerMap.get(typeArgument);
                }
            }
        }
        return null;
    }



    @Override
    public void setValueByProperty(Property property) {
        Function<Object, ?> handler = getHandler();
        if(handler != null) {
            List<Resource> list = (List<Resource>) handler.apply(property);
            setter.addValue(list);
        }
    }

    private List<Resource> getResourcesByProperty(Object property) {
        return resource.listProperties((Property) property)
                .toList().stream()
                .map(st -> st.getObject().asResource())
                .collect(Collectors.toList());
    }


}
