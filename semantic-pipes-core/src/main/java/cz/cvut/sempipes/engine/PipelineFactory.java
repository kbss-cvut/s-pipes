package cz.cvut.sempipes.engine;

import cz.cvut.sempipes.constants.KBSS_MODULE;
import cz.cvut.sempipes.constants.SM;
import cz.cvut.sempipes.constants.SML;
import cz.cvut.sempipes.modules.*;
import cz.cvut.sempipes.util.JenaPipelineUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.RDF;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Miroslav Blasko on 11.5.16.
 */
public class PipelineFactory {

    private static final Logger LOG = LoggerFactory.getLogger(PipelineFactory.class);

    // TODO inheritence not involved, not static context
    static Map<Resource, Class<? extends Module>> moduleTypes = new HashMap<>();

    //TODO move to ModuleRegistry
    static {
        moduleTypes.put(SML.ApplyConstruct, ApplyConstructModule.class);
        moduleTypes.put(SML.ExportToRDFFile, ExportToRDFFileModule.class);
        moduleTypes.put(SML.ImportFileFromURL, ImportFileFromURLModule.class);
        moduleTypes.put(SML.BindWithConstant, BindWithConstantModule.class);
        moduleTypes.put(SML.BindBySelect, BindBySelectModule.class);
        moduleTypes.put(SML.Merge, MergeModule.class);
        moduleTypes.put(SML.ReturnRDF, ReturnRDFModule.class);


        //kbss modules
        moduleTypes.put(KBSS_MODULE.tarql, TarqlModule.class);
        moduleTypes.put(KBSS_MODULE.form_generator, FormGeneratorModule.class);

    }


    public static void registerModuleType(Resource moduleType, Class<? extends Module> moduleClass) {
        moduleTypes.put(moduleType, moduleClass);
    }



    //TODO not here ?!
    public static Module loadModule(@NotNull Resource moduleRes) {

        // TODO multiple module types per resource
        Resource moduleTypeRes = moduleRes.getPropertyResourceValue(RDF.type);
        if (moduleTypeRes == null) {
            LOG.error("Cannot load module {} as its {} property value is missing.", moduleRes, RDF.type);
            return null;
        }
        return loadModule(moduleRes, moduleTypeRes);
    }

    // TODO not very effective
    public static Module loadPipeline(@NotNull Resource resource) {
        return loadPipelines(resource.getModel()).stream().filter(m -> {
            //TODO does not work on annonymous node
            if (resource.getURI().equals(m.getResource().getURI())) {
                return true;
            }
            return false;
        }).findAny().orElse(null);
    }

    /**
     * @param configModel
     * @return List of output modules.
     */
    public static List<Module> loadPipelines(@NotNull Model configModel) {

        // find and load all modules
        Map<Resource, Module> res2ModuleMap = new HashMap<>();

        JenaPipelineUtils.getAllModulesWithTypes(configModel)
                .entrySet().stream()
                .forEach(e -> {
                    Module m = loadModule(e.getKey(), e.getValue());
                    if (m != null) {
                        res2ModuleMap.put(e.getKey(), m);
                    }
                });
        //      .collect(Collectors.toMap(Map.Entry::getKey, e -> loadModule(e.getKey(), e.getValue())));


        // set appropriate links //TODO problem 2 files reusing module inconsistently ? do i need to solve it ?
        res2ModuleMap.entrySet().stream()
                .forEach(e -> {
                    Resource res = e.getKey();

                    // set up input modules
                    res.listProperties(SM.next).toList().stream()
                            .map(st -> {
                                Module m = res2ModuleMap.get(st.getObject().asResource());
                                if (m == null) {
                                    LOG.error("Ignoring statement {}. The object of the triple must have rdf:type {}.", st, SM.Module);
                                }
                                return m;
                            }).filter(m -> (m != null)).forEach(
                            m -> {

                                m.getInputModules().add(e.getValue());
                            }
                    );

                });

        Set<Module> inputModulesSet = res2ModuleMap.values().stream().flatMap(m -> m.getInputModules().stream()).collect(Collectors.toSet());

        List<Module> outputModulesList = res2ModuleMap.values().stream().collect(Collectors.toList());
        outputModulesList.removeAll(inputModulesSet);

        return outputModulesList;
    }

    private static Module loadModule(@NotNull Resource moduleRes, @NotNull Resource moduleTypeRes) {

        Class<? extends Module> moduleClass = moduleTypes.get(moduleTypeRes);

        if (moduleClass == null) {
            LOG.error("Ignoring module {}. Its type {} is not registered.", moduleRes, moduleTypeRes);
            return null;
        }

        Module module = null;

        try {
            module = moduleClass.newInstance();
            module.setConfigurationResource(moduleRes);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("Could not instantiate module of type " + moduleTypeRes, e);
        }

        return module;
    }

    public static Module loadModule(@NotNull Path configFilePath, @NotNull String moduleResourceUri) {
        // load config file
        Model configModel = ModelFactory.createDefaultModel();

        try {
            configModel.read(new FileInputStream(configFilePath.toFile()), null, FileUtils.langTurtle);
            return PipelineFactory.loadModule(configModel.createResource(moduleResourceUri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Loading of module failed.");
        }
    }


}
