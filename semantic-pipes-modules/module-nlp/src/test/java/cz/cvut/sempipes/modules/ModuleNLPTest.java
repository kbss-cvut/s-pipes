package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.engine.PipelineFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.ocpsoft.prettytime.nlp.PrettyTimeParser;
import org.ocpsoft.prettytime.nlp.parse.DateGroup;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * TODO test with blank nodes
 * Created by Miroslav Blasko on 22.8.16.
 */
public class ModuleNLPTest {



    @Ignore
    @Test
    public void executeSelf() throws Exception {

        ModuleNLP nlpModule = new ModuleNLP();

        // load input rdf
        Model model = ModelFactory.createDefaultModel();
        model.read(getClass().getResourceAsStream("/test2.ttl"), null, FileUtils.langTurtle);


        nlpModule.setInputContext(ExecutionContextFactory.createContext(model));

        ExecutionContext output = nlpModule.executeSelf();


        // execute module

        // check results

    }
}