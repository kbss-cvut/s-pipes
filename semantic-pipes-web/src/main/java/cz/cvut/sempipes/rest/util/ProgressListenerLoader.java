package cz.cvut.sempipes.rest.util;

import cz.cvut.sempipes.constants.SPIPES;
import cz.cvut.sempipes.engine.ProgressListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgressListenerLoader {
    private static final Logger LOG = LoggerFactory.getLogger(ProgressListenerLoader.class);

    public static List<ProgressListener> createListeners(Model configModel) {
        final List<ProgressListener> progressListeners = new LinkedList<>();

        configModel
            .listResourcesWithProperty(RDF.type, SPIPES.ProgressListener)
            .forEachRemaining(
                r -> {
                    String className = r.getRequiredProperty(SPIPES.has_classname).getString();
                    try {
                        Class<?> clazz = Class.forName(className);
                        Constructor<?> ctor = clazz.getConstructor(Resource.class);
                        ProgressListener pl = (ProgressListener) ctor.newInstance(new Object[] {r});
                        progressListeners.add(pl);
                    } catch (ClassNotFoundException | NoSuchMethodException |
                        InstantiationException | IllegalAccessException |
                        InvocationTargetException e) {

                        LOG.error("Could not construct progress listener of classname {}, cause: {}", className, e);
                        throw new RuntimeException("Could not construct progress listener of classname " + className + ".", e);
                    }
                }
            );
        return  progressListeners;
    }
}
