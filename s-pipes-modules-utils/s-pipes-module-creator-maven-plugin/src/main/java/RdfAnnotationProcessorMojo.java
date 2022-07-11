import cz.cvut.spipes.constants.SM;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.topbraid.spin.vocabulary.SPIN;
import org.topbraid.spin.vocabulary.SPL;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mojo(name = "process-annotations", defaultPhase = LifecyclePhase.COMPILE)
public class RdfAnnotationProcessorMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    MavenProject project;

    @Parameter(defaultValue = "${project.groupId}", readonly = true, required = true)
    String javaModuleName;

    @Parameter(required = true, readonly = true)
    String moduleClassName;

    @Parameter(required = true, readonly = true)
    String ontologyFilename;

    private final Log log = getLog();

    private final Class<cz.cvut.spipes.modules.Parameter> PARAM_ANNOTATION = cz.cvut.spipes.modules.Parameter.class;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            final Class<?> classObject = readModuleClass(moduleClassName);
            final var constraints = readConstraintsFromClass(classObject);
            writeConstraintsToOutputFile(constraints);
        } catch (Exception e) {
            log.error("Failed to execute s-pipes annotation processing: ", e);
            throw new MojoExecutionException("Exception during s-pipes execution", e);
        }
    }

    private Class<?> readModuleClass(String className) throws MalformedURLException, ClassNotFoundException {
        final File classesDirectory = new File(project.getBuild().getOutputDirectory());
        final URL classesUrl = classesDirectory.toURI().toURL();
        final URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{classesUrl}, getClass().getClassLoader());
        final Class<?> classObject = classLoader.loadClass(javaModuleName + "." + className);

        log.info("Successfully loaded SPipes Module: " + classObject.toGenericString());
        return classObject;
    }

    private List<cz.cvut.spipes.modules.Parameter> readConstraintsFromClass(Class<?> classObject) {
        return Arrays.stream(classObject.getDeclaredFields())
                .filter((field) -> field.isAnnotationPresent(PARAM_ANNOTATION))
                .map((field) -> field.getAnnotation(PARAM_ANNOTATION))
                .collect(Collectors.toUnmodifiableList());
    }

    private void writeConstraintsToOutputFile(List<cz.cvut.spipes.modules.Parameter> constraintAnnotations) throws FileNotFoundException {
        final var ontologyFolder = "/" + javaModuleName.replaceAll("[.]", "/") + "/";
        final var ontologyFilepath = project.getBuild().getOutputDirectory() + ontologyFolder + ontologyFilename;

        log.info("Reading ontology file: " + ontologyFilepath);
        final var model = ModelFactory.createDefaultModel();
        model.read(ontologyFilepath);
        final var statements = model.listStatements(null, RDF.type, SM.Module);
        while (statements.hasNext()) {
            final var statement = statements.next();
            final var subject = statement.getSubject();
            for (var annotation : constraintAnnotations) {
                final var modelConstraint = ResourceFactory.createResource();
                model.add(modelConstraint, RDF.type, SPL.Argument);
                model.add(modelConstraint, SPL.predicate, annotation.urlPrefix() + annotation.name());
                model.add(modelConstraint, RDFS.comment, "Automatically generated field: " + annotation.name());
                model.add(subject, SPIN.constraint, modelConstraint);

                log.info("Added model constraint based on annotation: " +
                        "(name = " + annotation.name() + ", urlPrefix = " + annotation.urlPrefix() + ")");
            }
        }
        model.write(new FileOutputStream(ontologyFilepath), "TTL");
        log.info("Successfully written constraints to the ontology file: " + ontologyFilepath);
    }
}
