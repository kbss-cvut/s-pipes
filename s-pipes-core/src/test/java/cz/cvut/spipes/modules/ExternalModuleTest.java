package cz.cvut.spipes.modules;


import cz.cvut.spipes.util.ExecUtils;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

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