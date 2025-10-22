package cz.cvut.spipes.engine;

import cz.cvut.spipes.constants.SM;
import cz.cvut.spipes.function.ARQFunction;
import cz.cvut.spipes.modules.Module;
import cz.cvut.spipes.util.JenaPipelineUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.function.FunctionRegistry;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.RDF;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class PipelineFactory {

    private static final Logger log = LoggerFactory.getLogger(PipelineFactory.class);

    // TODO inheritance not involved, not static context
    static final Map<Resource, Class<? extends Module>> moduleTypes = new HashMap<>();

    //TODO move to ModuleRegistry
    static {
        registerModuleTypesOnClassPath();
        registerFunctionsOnClassPath();
    }

    public static Map<Resource, Class<? extends Module>> getModuleTypes() {
        return moduleTypes;
    }

    /**
     * @deprecated modules are loaded automatically
     */
    @Deprecated
    public static void registerModuleType(Resource moduleType, Class<? extends Module> moduleClass) {
        _registerModuleType(moduleType,moduleClass);
    }

    private static void _registerModuleType(Resource moduleType, Class<? extends Module> moduleClass) {
        log.info(" module: {} -> {}", moduleType, moduleClass);
        moduleTypes.put(moduleType, moduleClass);
    }

    private static void _registerFunctionType(Resource functionType, Class<? extends ARQFunction> functionClass) {
        log.info(" function: {} -> {}", functionType, functionClass);
        FunctionRegistry.get().put(functionType.getURI(), functionClass);
    }


    public static void registerModuleTypesOnClassPath() {

        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage("cz.cvut.spipes.modules"))
                        .setScanners(new SubTypesScanner())
        );

        List<Class<? extends Module>> moduleClasses = reflections.getSubTypesOf(Module.class).stream().filter(
                c -> !Modifier.isAbstract(c.getModifiers())
        ).toList();

        moduleClasses.forEach(
                mClass -> {
                    String uri = instantiateModule(mClass).getTypeURI();
                    _registerModuleType(ResourceFactory.createResource(uri), mClass);
                }
        );

    }

    public static void registerFunctionsOnClassPath() {

        Reflections reflections = new Reflections(
            new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("cz.cvut.spipes.function"))
                .setScanners(new SubTypesScanner())
        );

        List<Class<? extends ARQFunction>> functionClasses = reflections.getSubTypesOf(ARQFunction.class).stream().filter(
            c -> !Modifier.isAbstract(c.getModifiers())
        ).toList();

        functionClasses.forEach(
            fClass -> {
                String uri = instantiateFunction(fClass).getTypeURI();
                _registerFunctionType(ResourceFactory.createResource(uri), fClass);//TODO ?
            }
        );

    }



    //TODO not here ?!
    public static Module loadModule(@NotNull Resource moduleRes) {

        // TODO multiple module types per resource
        Resource moduleTypeRes = moduleRes.getPropertyResourceValue(RDF.type);
        if (moduleTypeRes == null) {
            log.error("Cannot load module {} as its {} property value is missing.", moduleRes, RDF.type);
            return null;
        }
        return loadModule(moduleRes, moduleTypeRes);
    }

    // TODO not very effective
    public static Module loadPipeline(@NotNull Resource resource) {
        return loadPipelines(resource.getModel()).stream().filter(m -> {
            //TODO does not work on anonymous node
            return resource.getURI().equals(m.getResource().getURI());
        }).findAny().orElse(null);
    }

    /**
     * @param configModel configuration model containing pipeline definition
     * @return List of output modules.
     */
    public static List<Module> loadPipelines(@NotNull Model configModel) {

        Map<Resource, Module> res2ModuleMap = loadAllModules(configModel);

        Set<Module> inputModulesSet = res2ModuleMap.values().stream().flatMap(m -> m.getInputModules().stream()).collect(Collectors.toSet());

        List<Module> outputModulesList = new ArrayList<>(res2ModuleMap.values());
        outputModulesList.removeAll(inputModulesSet);

        return outputModulesList;
    }

    private static Map<Resource, Module> loadAllModules(@NotNull Model configModel) {

        // find and load all modules
        Map<Resource, Module> res2ModuleMap = new HashMap<>();

        JenaPipelineUtils.getAllModulesWithTypes(configModel)
                .forEach((key, value) -> {
                    Module m = loadModule(key, value);
                    if (m != null) {
                        res2ModuleMap.put(key, m);
                    }
                });
        //      .collect(Collectors.toMap(Map.Entry::getKey, e -> loadModule(e.getKey(), e.getValue())));


        // set appropriate links //TODO problem 2 files reusing module inconsistently ? do i need to solve it ?
        res2ModuleMap.forEach((res, value) -> {

            // set up input modules
            res.listProperties(SM.JENA.next).toList().stream()
                    .map(st -> {
                        Module m = res2ModuleMap.get(st.getObject().asResource());
                        if (m == null) {
                            log.error("Ignoring statement {}. The object of the triple must have rdf:type {}.", st, SM.Module);
                        }
                        return m;
                    }).filter(Objects::nonNull).forEach(
                            m -> m.getInputModules().add(value)
                    );

        });

        return res2ModuleMap;
    }

    private static Module loadModule(@NotNull Resource moduleRes, @NotNull Resource moduleTypeRes) {

        Class<? extends Module> moduleClass = moduleTypes.get(moduleTypeRes);

        if (moduleClass == null) {
            log.error("Ignoring module {}. Its type {} is not registered.", moduleRes, moduleTypeRes);
            return null;
        }

        Module module;

        try {
            module = moduleClass.getDeclaredConstructor().newInstance();
            module.setConfigurationResource(moduleRes);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalArgumentException("Could not instantiate module of type " + moduleTypeRes, e);
        }

        return module;
    }

    public static Module instantiateModule(Class<? extends Module> moduleClass) {
        try {
            return moduleClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalArgumentException("Could not instantiate module of type " +  moduleClass);
        }
    }

    public static ARQFunction instantiateFunction(Class<? extends ARQFunction> functionClass) {
        try {
            return functionClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalArgumentException("Could not instantiate function of type " +  functionClass);
        }
    }


    public static Module loadModule(@NotNull Path configFilePath, @NotNull String moduleResourceUri) {
        // load config file
        Model configModel = ModelFactory.createDefaultModel();

        try {
            configModel.read(new FileInputStream(configFilePath.toFile()), null, FileUtils.langTurtle);
            return PipelineFactory.loadModule(configModel.createResource(moduleResourceUri));
        } catch (FileNotFoundException e) {
            log.error("Loading of module failed.", e);
            throw new RuntimeException("Loading of module failed.");
        }
    }


    public static Module loadModulePipeline(Resource returnModuleRes) {
        return loadAllModules(returnModuleRes.getModel()).get(returnModuleRes);
    }
}
