package cz.cvut.spipes.rest.util;

import cz.cvut.spipes.constants.SPIPES;
import cz.cvut.spipes.engine.ProgressListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class ProgressListenerLoader {

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
                        log.info("Using progress listener: " + pl);
                    } catch (ClassNotFoundException | NoSuchMethodException |
                        InstantiationException | IllegalAccessException |
                        InvocationTargetException e) {

                        log.error("Could not construct progress listener of classname {}, cause: {}", className, e);
                        throw new RuntimeException("Could not construct progress listener of classname " + className + ".", e);
                    }
                }
            );
        return  progressListeners;
    }
}
