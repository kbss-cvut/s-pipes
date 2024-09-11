package cz.cvut.spipes.modules.handlers;

import cz.cvut.spipes.engine.ExecutionContext;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.topbraid.spin.model.Select;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;


/**
 * The `HandlerRegistry` class is a singleton responsible for managing and providing
 * handlers that can process different data types within the context of module configuration.
 * The registry allows for dynamic retrieval of the appropriate handler based on the class type.
 *
 * <p>Handlers are used to set values for fields in a module based on RDF resources.
 * The `HandlerRegistry` initializes and registers handlers for common data types,
 * and it provides a mechanism to register custom handlers as needed.
 *
 * <p>The registry is thread-safe, ensuring consistent behavior in multi-threaded environments.
 *
 * <p>Usage example:
 * <pre>
 * {@code
 * HandlerRegistry registry = HandlerRegistry.getInstance();
 * Handler<?> handler = registry.getHandler(String.class, resource, executionContext, setter);
 * }
 * </pre>
 */
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
        registerHandler(Boolean.class, BooleanHandler.class);
        registerHandler(boolean.class, BooleanHandler.class);
        registerHandler(Integer.class, IntegerHandler.class);
        registerHandler(int.class, IntegerHandler.class);
        registerHandler(String.class, StringHandler.class);
        registerHandler(RDFNode.class, RDFNodeHandler.class);
        registerHandler(Select.class, SelectHandler.class);
        registerHandler(URL.class, URLHandler.class);
        registerHandler(Path.class, PathHandler.class);
        registerHandler(Resource.class, ResourceHandler.class);
        registerHandler(List.class, ListHandler.class);
    }

    public synchronized Handler getHandler(Class clazz, Resource resource, ExecutionContext context, Setter setter) {
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


    /**
     * The `HandlerFactory` interface defines a factory for creating handler instances.
     */
    public interface HandlerFactory{
        Handler<?> getHandler(Resource resource, ExecutionContext executionContext, Setter setter);
    }

    /**
     * The `DefaultConstructorHandlerFactory` is a factory class that uses a constructor
     * to create handler instances. It implements the `HandlerFactory` interface.
     */
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
