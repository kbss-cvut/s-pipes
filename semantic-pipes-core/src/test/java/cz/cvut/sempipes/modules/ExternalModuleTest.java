package cz.cvut.sempipes.modules;


import cz.cvut.sempipes.util.ExecUtils;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Miroslav Blasko on 26.5.16.
 */
public class ExternalModuleTest {


    //@Test
    public void executeExternalProgram() throws Exception {

        String programCall = "/home/blcha/bin/rdf2rdf.sh -.ttl -.rdf";
        String[] programCallParams = programCall.split(" ");

        String DIR = "src/test/resources/module.apply-construct";
        Path path = Paths.get(DIR + "/standard-query-config.ttl");

        InputStream is = new FileInputStream(path.toFile());

        ExecUtils.execProgram(programCallParams, is);
    }





}