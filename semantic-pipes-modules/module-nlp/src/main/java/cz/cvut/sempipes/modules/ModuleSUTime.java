package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.KBSS_MODULE;
import cz.cvut.sempipes.constants.SML;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.TimeAnnotator;
import edu.stanford.nlp.time.TimeExpression;
import edu.stanford.nlp.util.CoreMap;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Created by Miroslav Blasko on 10.10.16.
 */
public class ModuleSUTime extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(ModuleSUTime.class);

    public static final String TYPE_URI = KBSS_MODULE.getURI()+"su-time";

    private List<Path> ruleFilePaths = new LinkedList<>();
    private String documentDate; // TODO support other formats ?


    public ModuleSUTime() {
    }

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

    @Override
    public void loadConfiguration() {

        if (this.resource.getProperty(SU_TIME.has_document_date) != null) { // TODO set current date if not specified
            documentDate = getEffectiveValue(SU_TIME.has_document_date).asLiteral().toString();
        }

        if (this.resource.getProperty(SU_TIME.has_rule_file) != null) { //TODO support more rule files
            ruleFilePaths.add(Paths.get(getEffectiveValue(SU_TIME.has_rule_file).asLiteral().toString()));
        }
    }

    @Override

    ExecutionContext executeSelf() {
        Model defaultModel = executionContext.getDefaultModel();

        AnnotationPipeline pipeline = loadPipeline();
        documentDate = "2016-10-10"; //TODO remove

        List<ReifiedStatement> temporalAnnotationStmts = new LinkedList<>();

        defaultModel.listStatements()
                .filterDrop(st -> !st.getObject().isLiteral())
                .toList().forEach(
                st -> {
                    String objectStr = st.getObject().asLiteral().getLexicalForm();

                    Annotation annotation = new Annotation(objectStr);
                    annotation.set(CoreAnnotations.DocDateAnnotation.class, "2016-10-10");
                    pipeline.annotate(annotation);

                    List<CoreMap> timexAnnsAll = annotation.get(TimeAnnotations.TimexAnnotations.class);
                    for (CoreMap cm : timexAnnsAll) {
                        List<CoreLabel> tokens = cm.get(CoreAnnotations.TokensAnnotation.class);

                        // create reification
                        ReifiedStatement reifiedSt = defaultModel.createReifiedStatement(st);

                        reifiedSt.addProperty(RDF.type, SU_TIME.sutime_extraction);
                        reifiedSt.addProperty(SU_TIME.has_character_offset_begin,
                                tokens.get(0).get(CoreAnnotations.CharacterOffsetBeginAnnotation.class).toString()); // TODO make xsd:int
                        reifiedSt.addProperty(SU_TIME.has_character_offset_end,
                                tokens.get(tokens.size() - 1).get(CoreAnnotations.CharacterOffsetEndAnnotation.class).toString());
                        reifiedSt.addProperty(SU_TIME.has_core_map,
                                cm.toString());
                        reifiedSt.addProperty(SU_TIME.has_temporal_expression,
                                cm.get(TimeExpression.Annotation.class).getTemporal().toString());

                        temporalAnnotationStmts.add(reifiedSt);

//                            LOG.trace("Recognized output: " + cm.get(TimeExpression.Annotation.class).getTemporal() + "<-- [from char offset " +
//                                    tokens.get(0).get(CoreAnnotations.CharacterOffsetBeginAnnotation.class) +
//                                    " to " + tokens.get(tokens.size() - 1).get(CoreAnnotations.CharacterOffsetEndAnnotation.class) + ']' +
//                                    " of string -- " + objectStr);


//                            LOG.trace("Recognized: " + objectStr + " [from char offset " +
//                                    tokens.get(0).get(CoreAnnotations.CharacterOffsetBeginAnnotation.class) +
//                                    " to " + tokens.get(tokens.size() - 1).get(CoreAnnotations.CharacterOffsetEndAnnotation.class) + ']' +
//                                    " --> " + cm.get(TimeExpression.Annotation.class).getTemporal());
                    }


                }
        );


        // TODO add "replace" attribute as in ApplyConstruct
        Model outputModel = ModelFactory.createDefaultModel();
        temporalAnnotationStmts.forEach(
                st -> outputModel.add(st.listProperties())
        );

        return ExecutionContextFactory.createContext(outputModel);
    }


    private AnnotationPipeline loadPipeline() {
        Properties props = new Properties();
        props.setProperty("sutime.rules", "sutime/defs.txt, sutime/defs.sutime.txt, sutime/english.holidays.sutime.txt, sutime/english.sutime.txt");
        AnnotationPipeline pipeline = new AnnotationPipeline();
        pipeline.addAnnotator(new TokenizerAnnotator(false));
        pipeline.addAnnotator(new WordsToSentencesAnnotator(false));
        pipeline.addAnnotator(new POSTaggerAnnotator(false));
        pipeline.addAnnotator(new TimeAnnotator("sutime", props));
        return pipeline;
    }
}
