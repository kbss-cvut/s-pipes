package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.exception.ScriptRuntimeErrorException;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.topbraid.spin.model.SPINFactory;
import org.topbraid.spin.util.SPINExpressions;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ListHandler extends Handler<List<?>> {

    public ListHandler(Resource resource, ExecutionContext executionContext, Setter<? super List<?>> setter) {
        super(resource, executionContext, setter);
    }

    @Override
    public void setValueByProperty(Property property) {
        setter.addValue(getRDFNodeListByProperty(property));
    }

    Class<?> getClazz() {
        Type genericType = setter.getField().getGenericType();
        if (genericType instanceof ParameterizedType parameterizedType) {
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (typeArguments.length > 0) {
                Type typeArgument = typeArguments[0];
                if (typeArgument instanceof Class<?>) {
                    return (Class<?>) typeArgument;
                }
            }
        }
        return null;
    }

    public List<?> getRDFNodeListByProperty(Object property) {
        Class<?> clazz = getClazz();
        HandlerRegistry handlerRegistry = HandlerRegistry.getInstance();
        BaseRDFNodeHandler<?> handler = (BaseRDFNodeHandler<?>) handlerRegistry.getHandler(clazz, resource, executionContext, setter);

        return resource.listProperties((Property) property)
                .toList().stream()
                .map(Statement::getObject)
                .map(x -> {
                    if (SPINExpressions.isExpression(x)) {
                        Resource expr = (Resource) SPINFactory.asExpression(x);
                        QuerySolution bindings = executionContext.getVariablesBinding().asQuerySolution();
                        return SPINExpressions.evaluate(expr, resource.getModel(), bindings);
                    } else {
                        return x;
                    }
                })
                .map(x -> {
                    try {
                        return handler.getRDFNodeValue(x);
                    } catch (Exception e) {
                        throw new ScriptRuntimeErrorException(
                                String.format("""
                            Failed to set value of the field `%s` of type `%s` within class `%s`.
                            The value was suppose to be converted from RDF node `%s` which was computed
                            from RDF statement <?s, ?p, ?o> where:
                            - s = `%s`,
                            - p = `%s`,
                            """,
                                        setter.getField().getName(),
                                        setter.getField().getType(),
                                        setter.getField().getDeclaringClass().getName(),
                                        x,
                                        resource,
                                        property
                                ),
                                e
                        );
                    }
                })
                .collect(Collectors.toList());
    }



}