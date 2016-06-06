package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.KBSS_MODULE;
import cz.cvut.sempipes.constants.SM;
import cz.cvut.sempipes.constants.SML;
import cz.cvut.sempipes.util.JenaModuleUtils;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.RDF;
import org.jetbrains.annotations.NotNull;
import org.topbraid.spin.util.JenaUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Miroslav Blasko on 11.5.16.
 */
public class PipelineFactory {

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

    //TODO not here ?!
    public static Module loadModule(Resource configResource) {

        // TODO multiple module types per resource
        Resource moduleTypeRes = configResource.getPropertyResourceValue(RDF.type);

        Class<? extends Module> moduleClass = moduleTypes.get(moduleTypeRes);

        Module module = null;

        try {
            module = moduleClass.newInstance();
            module.loadConfiguration(configResource);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return module;
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

    public static List<Module> loadPipelines(@NotNull Model configModel) {

        // find and load all modules
        Map<Resource, Module> res2ModuleMap = configModel.listResourcesWithProperty(RDF.type)
                .filterKeep(JenaModuleUtils::isModule).toList().stream()
                .collect(Collectors.toMap(Function.identity(), PipelineFactory::loadModule));

        // set appropriate links //TODO problem 2 files reusing module inconsistently ? do i need to solve it ?
        res2ModuleMap.entrySet().stream()
                .forEach(e -> {
                    Resource res = e.getKey();

                    e.getValue().getInputModules().addAll(
                            res.listProperties(SM.next).toList().stream()
                                    .map(res2ModuleMap::get).collect(Collectors.toList())
                    );
                });

        return res2ModuleMap.values().stream().collect(Collectors.toList());
    }

    public static Module loadModule(Path configFilePath, String moduleResourceUri) {
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
