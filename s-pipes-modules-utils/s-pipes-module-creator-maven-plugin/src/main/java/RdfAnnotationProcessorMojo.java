import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SM;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.topbraid.spin.vocabulary.SPIN;
import org.topbraid.spin.vocabulary.SPL;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Mojo(name = "process-annotations", defaultPhase = LifecyclePhase.COMPILE)
public class RdfAnnotationProcessorMojo extends AbstractMojo {

    private static final Map<String, String> DEFAULT_RDF_PREFIXES = Map.of(
            "kbss-module", KBSS_MODULE.uri,
            "owl", "http://www.w3.org/2002/07/owl#",
            "rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
            "rdfs", "http://www.w3.org/2000/01/rdf-schema#",
            "sm", "http://topbraid.org/sparqlmotion#",
            "spin", "http://spinrdf.org/spin#",
            "spl", "http://spinrdf.org/spl#",
            "xsd", "http://www.w3.org/2001/XMLSchema#"
    );
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    MavenProject project;

    @Parameter(defaultValue = "${project.groupId}", readonly = true, required = true)
    String javaModuleName;

    @Parameter(required = false, readonly = true)
    String moduleClassName;

    @Parameter(required = false, readonly = true)
    String modulePackageName;

    @Parameter(required = false, readonly = true)
    String ontologyFilename;

    @Parameter(required = false, readonly = true)
    GenerationMode mode;

    enum GenerationMode {
        RDF_FOR_MODULE,
        RDF_FOR_ALL_CHILDREN
    }

    private final Class<cz.cvut.spipes.modules.Parameter> PARAM_ANNOTATION = cz.cvut.spipes.modules.Parameter.class;

    private final Class<SPipesModule> MODULE_ANNOTATION = cz.cvut.spipes.modules.annotations.SPipesModule.class;


    //region Entrypoint methods
    @Override
    public void execute() throws MojoExecutionException {
        if (mode == null) {
            final var defaultMode = GenerationMode.RDF_FOR_ALL_CHILDREN;
            getLog().warn(String.format("No generation mode is specified, defaulting to %s. Available modes: %s",
                    defaultMode, Arrays.toString(GenerationMode.values())));
            mode = defaultMode;
        }
        getLog().info("Executing in the following mode: " + mode.name());

        try {
            switch (mode) {
                case RDF_FOR_MODULE:
                    generateRdfForModule();
                    return;
                case RDF_FOR_ALL_CHILDREN:
                    generateRdfForAllModules();
                    return;
                default:
                    throw new MojoExecutionException("Generation mode not specified");
            }
        } catch (RuntimeException | IOException | ReflectiveOperationException e) {
            throw new MojoExecutionException("Unexpected exception during execution", e);
        }

    }

    private void generateRdfForAllModules() throws MalformedURLException, ClassNotFoundException, FileNotFoundException {
        //read all submodules
        getLog().info("Generating an RDF for all sub-modules");
        var model = initDefaultModel();
        for (MavenProject submodule : (List<MavenProject>) project.getCollectedProjects()) {

            //find module's main class
            var moduleClasses = readAllModuleClasses(submodule);
            getLog().info("Module: " + submodule.getName() + " | Classes: [" + moduleClasses.stream()
                    .map(Class::getSimpleName)
                    .collect(Collectors.joining(", ")) + "]");

            //add module to the RDF structure
            for (Class<?> moduleClass : moduleClasses) {
                getLog().info("Creating RDF for module '" + moduleClass.getCanonicalName() + "'");
                var moduleAnnotation = readModuleAnnotationFromClass(moduleClass);
                var constraints = readConstraintsFromClass(moduleClass);
                writeConstraintsToModel(model, constraints, moduleAnnotation);
            }

            getLog().info("--------------------------------------");
        }
        writeModelToStdout(model);
        try {
            writeToTargetFolderFile(model, "all-modules.ttl");
        } catch (IOException e) {
            getLog().error("Failed to write model to the output file", e);
        }
        getLog().info("======================================");
    }

    private void generateRdfForModule() throws MojoExecutionException {
        try {
            Set<Class<?>> moduleClasses = readAllModuleClasses(this.project);
            var model = readModelFromDefaultFile();
            for (Class<?> moduleClass : moduleClasses) {
                var moduleAnnotation = readModuleAnnotationFromClass(moduleClass);
                var constraints = readConstraintsFromClass(moduleClass);
                writeConstraintsToModel(model, constraints, moduleAnnotation);
            }

            var ontologyPath = modulePackageName.replaceAll("[.]", "/") + "/" + ontologyFilename;
            writeToTargetFolderFile(model, ontologyPath);
        } catch (Exception e) {
            getLog().error("Failed to execute s-pipes annotation processing: ", e);
            throw new MojoExecutionException("Exception during s-pipes execution", e);
        }
    }
    //endregion

    //region Parsing Java classes
    private Set<Class<?>> readAllModuleClasses(MavenProject project) throws MalformedURLException, ClassNotFoundException {
        //Configure the class searcher
        final File classesDirectory = new File(project.getBuild().getOutputDirectory());
        final URL classesUrl = classesDirectory.toURI().toURL();
        final URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{classesUrl}, getClass().getClassLoader());
        var reflectionConfig = new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forClassLoader(classLoader))
                .setScanners(new SubTypesScanner(false), new TypeAnnotationsScanner())
                .filterInputsBy(new FilterBuilder().includePackage(modulePackageName));
        var classSearcher = new Reflections(reflectionConfig);

        //Find classes with the module annotation
        Set<Class<?>> moduleClasses = new HashSet<>();
        for (String type : classSearcher.getAllTypes()) {
            final Class<?> classObject = classLoader.loadClass(type);
            getLog().debug("Class: " + type + " | Annotations: ["
                    + Arrays.stream(classObject.getAnnotations())
                    .map(Annotation::annotationType)
                    .map(Class::getSimpleName)
                    .collect(Collectors.joining(", ")) + "]");

            if (classObject.isAnnotationPresent(MODULE_ANNOTATION)) {
                moduleClasses.add(classObject);
            }
        }
        return moduleClasses;
    }

    private SPipesModule readModuleAnnotationFromClass(Class<?> classObject) {
        return classObject.getAnnotation(MODULE_ANNOTATION);
    }

    private List<cz.cvut.spipes.modules.Parameter> readConstraintsFromClass(Class<?> classObject) {
        return Arrays.stream(classObject.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(PARAM_ANNOTATION))
                .map(field -> field.getAnnotation(PARAM_ANNOTATION))
                .collect(Collectors.toUnmodifiableList());
    }
    //endregion

    //region Working with RDF

    public Model initDefaultModel() {
        var model = ModelFactory.createDefaultModel();

        model.setNsPrefixes(DEFAULT_RDF_PREFIXES);

        var ontology = ResourceFactory.createResource(KBSS_MODULE.uri + "all-modules-ontology");
        model.add(ontology, RDF.type, OWL.Ontology);
        return model;
    }

    private Model readModelFromDefaultFile() {
        var ontologyFolder = "/" + modulePackageName.replaceAll("[.]", "/") + "/";
        var ontologyFilepath = project.getBuild().getOutputDirectory() + ontologyFolder + ontologyFilename;
        var model = ModelFactory.createDefaultModel();
        model.read(ontologyFilepath);
        getLog().info("Successfully read the existing ontology file: " + ontologyFilepath);
        return model;
    }

    private void writeConstraintsToModel(Model baseRdfModel,
                                         List<cz.cvut.spipes.modules.Parameter> constraintAnnotations,
                                         SPipesModule moduleAnnotation) {
        final var root = ResourceFactory.createResource(KBSS_MODULE.uri + moduleAnnotation.label().replaceAll(" ", "-").toLowerCase()); //todo can be added to the annotation
        baseRdfModel.add(root, RDF.type, SM.Module);
        baseRdfModel.add(root, RDFS.comment, moduleAnnotation.comment());
        baseRdfModel.add(root, RDFS.label, moduleAnnotation.label());
        final var statements = baseRdfModel.listStatements(null, RDF.type, SM.Module);
        while (statements.hasNext()) {
            final var statement = statements.next();
            final var subject = statement.getSubject();
            for (var annotation : constraintAnnotations) {
                final var modelConstraint = ResourceFactory.createResource();
                baseRdfModel.add(modelConstraint, RDF.type, SPL.Argument);
                baseRdfModel.add(modelConstraint, SPL.predicate, annotation.urlPrefix() + annotation.name());
                baseRdfModel.add(modelConstraint, RDFS.comment, "Automatically generated field: " + annotation.name());
                baseRdfModel.add(subject, SPIN.constraint, modelConstraint);

                getLog().debug("Added model constraint based on annotation: " +
                        "(name = " + annotation.name() + ", urlPrefix = " + annotation.urlPrefix() + ")");
            }
        }
    }

    private void writeToTargetFolderFile(Model model, String outputFilename) throws IOException {
        var filepath = Path.of(project.getBuild().getOutputDirectory() + "/" + outputFilename);
        if (Files.notExists(filepath)) {
            getLog().debug("File does not exist, will create anew: " + filepath);
            var dirPath = Path.of(project.getBuild().getOutputDirectory());
            if (Files.notExists(dirPath)) {
                getLog().debug("Not all directories in the provided path exist, will create them as necessary: " + dirPath);
                Files.createDirectories(dirPath);
            }
            Files.createFile(filepath);
        }

        model.write(Files.newOutputStream(filepath), FileUtils.langTurtle);
        getLog().info("Successfully written constraints to file: " + filepath);
    }

    private void writeModelToStdout(Model model) {
        var out = new ByteArrayOutputStream();
        model.write(out, FileUtils.langTurtle);

        getLog().info("Generated RDF:\n" +
                "-----------------------------------\n" +
                out +
                "-----------------------------------\n");
    }
    //endregion
}
