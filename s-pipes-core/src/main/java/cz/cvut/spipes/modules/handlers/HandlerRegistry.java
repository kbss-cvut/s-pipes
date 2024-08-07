package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.spin.model.Select;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;



public class HandlerRegistry {

    private static HandlerRegistry instance;
    private final Map<Class, HandlerFactory> handlers =  Collections.synchronizedMap(new HashMap<>());

    public synchronized static HandlerRegistry getInstance() {
        if (instance == null) {
            instance = new HandlerRegistry();
        }
        return instance;
    }

    private HandlerRegistry() {
        initHandlers();
    }

    private void initHandlers() {
        registerHandler(boolean.class, BooleanHandler.class);
        registerHandler(Integer.class, IntegerHandler.class);
        registerHandler(String.class, StringHandler.class);
        registerHandler(RDFNode.class, RDFNodeHandler.class);
        registerHandler(Select.class, SelectHandler.class);
        registerHandler(URL.class, URLHandler.class);
        registerHandler(PathHandler.class, PathHandler.class);
    }

    public synchronized Handler<?> getHandler(Class clazz, Resource resource, ExecutionContext context, Setter setter) {
        HandlerFactory handlerFactory = handlers.get(clazz);
        if (handlerFactory == null) {
            throw new RuntimeException("No handler for " + clazz);
        }
        return handlerFactory.getHandler(resource, context, setter);
    }


    private static Constructor<? extends Handler> getConstructor(Class<? extends Handler> handler){
        try {
            return handler.getConstructor(Resource.class, ExecutionContext.class, Setter.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("No suitable constructor found for handler " + handler);
        }
    }

    public synchronized void registerHandler(Class valueType, Class<? extends Handler> handlerClass) {
        handlers.put(valueType, new DefaultConstructorHandlerFactory(handlerClass));
    }


    public interface HandlerFactory{
        Handler<?> getHandler(Resource resource, ExecutionContext executionContext, Setter setter);
    }

    private class DefaultConstructorHandlerFactory implements HandlerFactory {

        private final Constructor<? extends Handler> constructor;

        public DefaultConstructorHandlerFactory(Class type) {
            this.constructor = getConstructor(type);
        }

        @Override
        public Handler<?> getHandler(Resource resource, ExecutionContext executionContext, Setter setter) {
            try {
                return constructor.newInstance(resource, executionContext, setter);
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
