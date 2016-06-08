package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.AppConstants;
import cz.cvut.sempipes.constants.SML;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.util.ExecUtils;
import org.apache.jena.ext.com.google.common.io.Files;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.arq.ARQFactory;
import org.topbraid.spin.model.Construct;
import org.topbraid.spin.system.SPINModuleRegistry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Miroslav Blasko on 26.5.16.
 */
public class TarqlModule extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(TarqlModule.class);
    //tarql [options] query.sparql [table.csv [...]]

    private static final String TARQL_PROGRAM = AppConstants.BIN_DIR + "/tarql";

    //sml:constructQuery
    private List<Resource> constructQueries;

    private String tableFilePath;

    //sml:replace
    private boolean isReplace;

    //sml:sourceFilePath
    private String sourceFilePath;

    public TarqlModule() {
        // TODO move elsewhere
        SPINModuleRegistry.get().init();
    }


    @Override
    public ExecutionContext execute() {

        //Model defaultInputModel = context.getDefaultModel();

        // TODO full external context support
        // set variable binding

        Path p = Paths.get("/home/blcha/sempipes-log.txt");

        Model mergedModel = ModelFactory.createDefaultModel();
        if (! isReplace) { // TODO mozno sa mozu zmenit blank nody (asi by som mal mergovat do defaultneho modelu ?)
            if (executionContext.getDefaultModel() != null) {
                mergedModel.add(executionContext.getDefaultModel());
            }
        }

        //      set up variable bindings
        for (Resource constructQueryRes : constructQueries) {
            Construct spinConstructRes = constructQueryRes.as(Construct.class);

            Query query = ARQFactory.get().createQuery(spinConstructRes);

            // save string query to temporary file
            File tempFile = ExecUtils.createTempFile();
            try {
                Files.append(query.toString(), tempFile, Charset.defaultCharset());
            } catch (IOException e) {
                e.printStackTrace();
            }

            // execute tarql query.sparql table.csv
            String[] programCall = new String[]{
                    TARQL_PROGRAM,
                    tempFile.getAbsolutePath(),
                    Paths.get(sourceFilePath).toAbsolutePath().toString()
            };


            InputStream is = ExecUtils.execProgramWithoutExeption(programCall, null);

            // merge output to model
            mergedModel.read(is, null, FileUtils.langTurtle);
        }

        //newModel.write(System.out, FileUtils.langTurtle);

        //TODO should return only Model ???
        ExecutionContext ec = ExecutionContextFactory.createContext(mergedModel);
        return ec;
    }

    @Override
    public void loadConfiguration() {
        // TODO load default values from configuration

        // TODO does not work with string query as object is not RDF resource ???
        constructQueries = resource
                .listProperties(SML.constructQuery)
                .toList().stream()
                .map(st -> st.getObject().asResource())
                .collect(Collectors.toList());

        LOG.debug("Loading spin constuct queries ... " + constructQueries);

        //TODO default value must be taken from template definition
        isReplace = this.getPropertyValue(SML.replace, false);

        sourceFilePath = this.getStringPropertyValue(SML.sourceFilePath);
    }




}
