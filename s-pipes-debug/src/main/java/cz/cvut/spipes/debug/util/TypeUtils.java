package cz.cvut.spipes.debug.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.springframework.core.MethodParameter;

public class TypeUtils {
    public static Class<?> getListElementType(MethodParameter methodParameter) {
        Type genericType = methodParameter.getGenericParameterType();
        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (typeArguments.length > 0) {
                Type typeArgument = typeArguments[0];
                if (typeArgument instanceof Class) {
                    return (Class<?>) typeArgument;
                }
            }
        }
        return null;
    }
}
