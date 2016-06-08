package cz.cvut.sempipes.modules;


import cz.cvut.sempipes.util.ExecUtils;
import org.junit.Test;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.apache.jena.sparql.vocabulary.DOAP.os;
import static org.junit.Assert.*;

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