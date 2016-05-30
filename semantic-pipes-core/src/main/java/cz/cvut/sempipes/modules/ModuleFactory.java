package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.Constants;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextImpl;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.RDF;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Miroslav Blasko on 11.5.16.
 */
public class ModuleFactory {

    // TODO inheritence not involved, not static context
    static Map<String, Class<? extends Module>> moduleTypes = new HashMap<>();

    //TODO move to ModuleRegistry
    static {
        moduleTypes.put(Constants.SML_APPLY_CONSTRUCT, ApplyConstructModule.class);
        moduleTypes.put(Constants.SML_EXPORT_TO_RDF_FILE, ExportToRDFFileModule.class);
        moduleTypes.put(Constants.SML_IMPORT_FILE_FROM_URL, ImportFileFromURLModule.class);
        moduleTypes.put(Constants.SML_BIND_WITH_CONSTANT, BindWithConstantModule.class);
        moduleTypes.put(Constants.SML_BIND_BY_SELECT, BindBySelectModule.class);
        moduleTypes.put(Constants.SML_MERGE, MergeModule.class);
        moduleTypes.put(Constants.SML_RETURN_RDF, ReturnRDFModule.class);

        //kbss modules
        moduleTypes.put(Constants.KBSS_MODULE_TARQL, TarqlModule.class);
        moduleTypes.put(Constants.KBSS_MODULE_FORM_GENERATOR, FormGeneratorModule.class);

    }

    //TODO not here ?!
    public static Module loadModule(Resource configResource)  {

        // TODO multiple module types per resource
        Resource moduleTypeRes = configResource.getPropertyResourceValue(RDF.type);

        Class<? extends Module> moduleClass = moduleTypes.get(moduleTypeRes.getURI());

        Module module = null;

        try {
            module = moduleClass.newInstance();
            module.loadConfiguration(configResource);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return module;
    }


    public static Module loadPipeline(Resource resource) {
        throw new RuntimeException("Not implemented.");
    }

    public static Module loadModule(Path configFilePath, String moduleResourceUri) {
        // load config file
        Model configModel = ModelFactory.createDefaultModel();

        try {
            configModel.read(new FileInputStream(configFilePath.toFile()), null, FileUtils.langTurtle);
            return ModuleFactory.loadModule(configModel.createResource(moduleResourceUri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Loading of module failed.");
        }
    }
}
