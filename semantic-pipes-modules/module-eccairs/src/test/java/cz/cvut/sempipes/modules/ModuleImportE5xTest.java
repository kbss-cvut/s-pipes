package cz.cvut.sempipes.modules;

import cz.cvut.kbss.eccairs.cfg.Configuration;
import cz.cvut.kbss.eccairs.report.e5xml.e5x.E5XXMLParser;
import cz.cvut.kbss.eccairs.report.model.EccairsReport;
import cz.cvut.kbss.eccairs.report.model.dao.EccairsReportDao;
import cz.cvut.kbss.eccairs.schema.dao.SingeltonEccairsAccessFactory;
import cz.cvut.kbss.eccairs.schema.dao.cfg.EccairsAccessConfiguration;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import cz.cvut.kbss.jopa.model.descriptors.EntityDescriptor;
import cz.cvut.kbss.ucl.MappingEccairsData2Aso;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.modules.eccairs.SesameDataDao;
import cz.cvut.sempipes.modules.eccairs.JopaPersistenceUtils;
import cz.cvut.sempipes.registry.StreamResource;
import cz.cvut.sempipes.registry.StringStreamResource;
import cz.cvut.sempipes.util.JenaUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.repository.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

/**
 * Created by Miroslav Blasko on 28.11.16.
 */
public class ModuleImportE5xTest {

    private static final String FILE_NAME_PATH = "/data/16FEDEF0BC91E511B897002655546824-anon.xml";

    private static StreamResource streamResouce;

    @BeforeClass
    public static void setUp() throws IOException {
        String e5xFilePath = ModuleImportE5xTest.class.getResource(FILE_NAME_PATH).getPath();
        String fileContent = new String(Files.readAllBytes(Paths.get(e5xFilePath)));

        streamResouce =  new StringStreamResource(e5xFilePath, fileContent);
    }

    @Ignore
    @Test
    public void executeSelf() throws Exception {
        ModuleImportE5x module = new ModuleImportE5x();

        module.setInputContext(ExecutionContextFactory.createEmptyContext());
        module.setE5xResource(streamResouce);

        module.setComputeEccairsToAviationSafetyOntologyMapping(false);
        ExecutionContext outputContextWithoutMapping = module.executeSelf();
        long modelSizeWithoutMapping = outputContextWithoutMapping.getDefaultModel().size();

        module.setComputeEccairsToAviationSafetyOntologyMapping(true);
        ExecutionContext outputContextWithMapping = module.executeSelf();
        long modelSizeWithMapping = outputContextWithMapping.getDefaultModel().size();

        assertTrue(modelSizeWithoutMapping > 0);
        assertTrue(modelSizeWithoutMapping < modelSizeWithMapping);
    }

}