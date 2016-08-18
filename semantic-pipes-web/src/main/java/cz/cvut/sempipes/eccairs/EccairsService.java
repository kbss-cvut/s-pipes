package cz.cvut.sempipes.eccairs;

import cz.cvut.sempipes.constants.KBSS_MODULE;
import cz.cvut.sempipes.engine.*;
import cz.cvut.sempipes.manager.OntoDocManager;
import cz.cvut.sempipes.modules.Module;
import cz.cvut.sempipes.modules.ModuleSesame;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.topbraid.spin.system.SPINModuleRegistry;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;


/**
 * Created by Miroslav Blasko on 9.6.16.
 */
public class EccairsService {

    private static final Logger LOG = LoggerFactory.getLogger(EccairsService.class);
    private static Path inbasModelPath = Paths.get(ConfigProperies.get("inbasModelDir"));
    private static Model libsModel = ModelFactory.createDefaultModel();

    public String run(final InputStream rdfData, String contentType, final MultiValueMap parameters) {
        if (Boolean.valueOf(ConfigProperies.get("returnSampleFlag"))) {
            return returnSampleFile();
        } else {
            return  runGeneratorService(parameters);
        }

    }

    //static {
    //   loadEccairsModel();
    //}

    private static void loadEccairsModel() {
        LOG.info("Constructing library modules ...");

        libsModel.add(OntoDocManager.loadModel(getInbasModelFilePath("eccairsFormGeneratorPath")));
        String[] relativePaths = new String[]{
                "lib",
                "forms/eccairs-0.2/eccairs-form-lib.ttl"
        };

        Arrays.asList(relativePaths).forEach(relPath -> {
            OntoDocManager.getAllFile2Model(inbasModelPath.resolve(relPath)).values().forEach(
                    libsModel::add
            );
        });

        SPINModuleRegistry.get().registerAll(libsModel, null);

    }

    private static void clearEccairsModel() {
        libsModel = ModelFactory.createDefaultModel();
    }



    public String runGeneratorService(final MultiValueMap parameters) {
        LOG.info("Running generator service with parameters = {}", parameters);

//        OntologyManager.getFileManager().setModelCaching(true);
//        OntologyManager.loadAllBaseIrisFromDir(inbasModelPath.resolve("lib"));
//        OntologyManager.ignoreImport("http://onto.fel.cvut.cz/ontologies/aviation/eccairs-form-static-0.2");
//        OntModel model = OntologyManager.loadOntModel(getInbasModelFilePath("eccairsFormGeneratorPath"));


        LOG.warn("!!! temporary hack - reload model each service call (should be removed)"); //TODO !!! remove this hack
        clearEccairsModel();
        loadEccairsModel(); // TODO enable it in static part of the class
        PipelineFactory.registerModuleType(KBSS_MODULE.deploy, ModuleSesame.class);

        Model mergedModel = ModelFactory.createDefaultModel();
        mergedModel.add(libsModel);


        String id = (String) parameters.getFirst("id");

        Module module = null;
        switch (id) {
            case "fssStudyTestForms":
            case "deploy-question-templates":
                LOG.info("Running deploy-question-templates module");
                LOG.info("Registering {} -> {}", KBSS_MODULE.deploy, ModuleSesame.class);
                PipelineFactory.registerModuleType(KBSS_MODULE.deploy, ModuleSesame.class);
                module = PipelineFactory.loadPipeline(mergedModel.getResource(ConfigProperies.get("deployQueryTemplatesModule")));
                break;
            case "generateEccairsForms":
                LOG.info("Running eccairs service module");
                module = PipelineFactory.loadPipeline(mergedModel.getResource(ConfigProperies.get("eccairsServiceModule")));
                break;
        }



        // TODO service definition workaround -- all parameters are visible in all modules => must be unique
        ExecutionContext context = ExecutionContextFactory.createContext(ModelFactory.createDefaultModel(), new VariablesBinding(transform(parameters)));

        ExecutionEngine engine = ExecutionEngineFactory.createEngine();
        ExecutionContext newContext = engine.executePipeline(module, context);

        return getJsonLdSerialization(newContext.getDefaultModel());
    }


    /**
     * Returns sample eccairs form file.
     *
     * @param rdfData
     * @param contentType
     * @param parameters
     * @return
     */
    public String returnSampleFile() {

        Path sampleFormPath = getInbasModelFilePath("sampleFormPath");

        Model m = ModelFactory.createDefaultModel();
        try {
            m.read(new FileInputStream(sampleFormPath.toFile()), null, FileUtils.langTurtle);

            return getJsonLdSerialization(m);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


//    private static String run(final InputStream rdfData, String contentType, final MultiValueMap parameters) {
//        LOG.info("- parameters={}", parameters);
//
//        if (!parameters.containsKey(P_ID)) {
//            throw new SempipesServiceNoModuleIdException();
//        }
//
//        final String id = parameters.getFirst(P_ID).toString();
//        LOG.info("- id={}", id);
//
//        final Map moduleParams = parameters.toSingleValueMap();
//        moduleParams.remove(P_ID);
//
//        final QuerySolution querySolution = transform(moduleParams);
//        LOG.info("- parameters as query solution ={}", querySolution);
//        contentType = contentType == null || contentType.isEmpty() ? "application/n-triples" : contentType;
//
//        // TODO find in module registry ?!?
//        String result = "";
//        if (id.equals("http://onto.fel.cvut.cz/ontologies/sempipes/identity-transformer")) {
//            Model m = ModelFactory.createDefaultModel();
//            m.read(rdfData, "", RDFLanguages.contentTypeToLang(contentType).getLabel());
//            final StringWriter writer = new StringWriter();
////            m.write(writer);
//            RDFDataMgr.write(writer, m, Lang.JSONLD);
//            result = writer.toString();
//        } else {
//            throw new SempipesServiceInvalidModuleIdException();
//        }
//
//        LOG.info("Processing successfully finished.");
//        return result;
//    }


    private String getJsonLdSerialization(Model model) {

        //final StringWriter writer = new StringWriter();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        RDFDataMgr.write(os, model, RDFFormat.JSONLD);


        //m.write(writer, FileUtils.langTurtle)
        return new String(os.toByteArray());
    }

    private static Path getInbasModelFilePath(String relativePath) {
        return Paths.get(ConfigProperies.get("inbasModelDir"), ConfigProperies.get(relativePath));
    }

    private QuerySolution transform(final Map parameters) {
        final QuerySolutionMap querySolution = new QuerySolutionMap();

        for (Object key : parameters.keySet()) {
            // TODO types of RDFNode
            String value = (String) ((LinkedList) parameters.get(key)).get(0);
            querySolution.add(key.toString(), ResourceFactory.createPlainLiteral(value));
        }

        return querySolution;
    }
}
