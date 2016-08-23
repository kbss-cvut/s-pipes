package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.KBSS_MODULE;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import cz.cvut.sempipes.engine.PipelineFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.ocpsoft.prettytime.nlp.PrettyTimeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.List;



public class ModuleNLP extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(ModuleNLP.class);
    final Property HAS_DATE = ResourceFactory.createProperty("http://kbss/has-date");


    static {
        LOG.info("Registering {} -> {}", KBSS_MODULE.nlp, ModuleNLP.class);
        PipelineFactory.registerModuleType(KBSS_MODULE.nlp, ModuleNLP.class);
    }

    // TODO  ?! does not work with blank-nodes
    @Override
    ExecutionContext executeSelf() {



        Model defaultModel = executionContext.getDefaultModel();




        ExtendedIterator<Statement> stmtIt = defaultModel.listStatements().filterKeep(
                st -> {
                    if (! st.getObject().isLiteral()) {
                        return false;
                    }
                    return true;
                }
        );

        while (stmtIt.hasNext()) {
            Statement st = stmtIt.next();

            String lexicalForm = st.getObject().asLiteral().getLexicalForm();

            List<Date> parsedDates = new PrettyTimeParser().parse(lexicalForm);

            if (! parsedDates.isEmpty()) {
                System.out.println("Lexical form: " + lexicalForm);
            }

            ReifiedStatement reification = st.createReifiedStatement();

            parsedDates.forEach(
                    date -> {
                        Literal dateNode = getDateTimeRdfNode(date, reification.getModel());
                        System.out.println("\t date: " + dateNode);

                        reification.addProperty(HAS_DATE, dateNode);
                    }
            );
        }

        // collect reifications

        // return ExecutionContextFactory.createContext(mergedModel);
        // return executionContext;
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public void loadConfiguration() {
    }

    // TODO is it good enough like this ? http://stackoverflow.com/questions/24978636/jena-storing-date-in-xml-datetime-unexpected-behaviour
    private Literal getDateTimeRdfNode(Date date, Model model) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        return model.createTypedLiteral(cal);
    }
}
