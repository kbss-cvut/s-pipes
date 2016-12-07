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

import java.io.*;
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

    static String e5xFilePath;
    static String fileContent;

    @Test
    public void executeSelfWithXml() throws Exception {
        String e5xFilePath = ModuleImportE5xTest.class.getResource( "/data/16FEDEF0BC91E511B897002655546824-anon.xml").getPath();
//        String fileContent = new String(Files.readAllBytes(Paths.get(e5xFilePath)));
        StreamResource streamResouce =  new StringStreamResource(e5xFilePath, Files.readAllBytes(Paths.get(e5xFilePath)), "text/xml");

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

    @Test
    public void executeSelfWithZip() throws Exception {
        String e5xFilePath = ModuleImportE5xTest.class.getResource( "/data/16FEDEF0BC91E511B897002655546824.e5x").getPath();
        final File file = Paths.get(e5xFilePath).toFile();
//        byte[] encoded = Base64.encodeBase64(org.apache.commons.io.FileUtils.readFileToByteArray(file));
//        String fileContent = new String(encoded, StandardCharsets.US_ASCII);
//        String e5xFilePath = ModuleImportE5xTest.class.getResource( "/data/16FEDEF0BC91E511B897002655546824.e5x").getPath();
//        String fileContent = new String(Files.readAllBytes(Paths.get(e5xFilePath)));
//        StreamResource streamResouce =  new StringStreamResource(e5xFilePath, fileContent, "application/zip");
        InputStream is = new FileInputStream(file);
        StreamResource streamResouce = new StringStreamResource(e5xFilePath, Files.readAllBytes(Paths.get(e5xFilePath)), "application/zip");

        ModuleImportE5x module = new ModuleImportE5x();

        module.setInputContext(ExecutionContextFactory.createEmptyContext());
        module.setE5xResource(streamResouce);

        module.setComputeEccairsToAviationSafetyOntologyMapping(false);
        ExecutionContext outputContextWithoutMapping = module.executeSelf();
        long modelSizeWithoutMapping = outputContextWithoutMapping.getDefaultModel().size();

        module.setE5xResource(streamResouce);
        module.setComputeEccairsToAviationSafetyOntologyMapping(true);
        ExecutionContext outputContextWithMapping = module.executeSelf();
        long modelSizeWithMapping = outputContextWithMapping.getDefaultModel().size();

        assertTrue(modelSizeWithoutMapping > 0);
        assertTrue(modelSizeWithoutMapping < modelSizeWithMapping);
    }
}