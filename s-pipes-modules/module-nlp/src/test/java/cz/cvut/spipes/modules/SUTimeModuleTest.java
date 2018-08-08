package cz.cvut.spipes.modules;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.time.*;
import edu.stanford.nlp.util.CoreMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Miroslav Blasko on 10.10.16.
 */
public class SUTimeModuleTest {
    @Test
    public void loadConfiguration() throws Exception {
    }

    @Test
    public void executeSelf() throws Exception {

    }


    @Test
    public void simpleTest() throws Exception {
        //String ss = "od 08:00 do 16:00 hodin";
        //String ss = "I should be in school tomorrow by 9 o'clock.";
        //String ss = "N 264/2008";
        String ss = "č. 130/2008 Sb.";
        //String ss = "č 130/2008 Sb.";
        Properties props = new Properties();
        props.setProperty("sutime.rules", "sutime/defs.txt, sutime/defs.sutime.txt, sutime/english.holidays.sutime.txt, sutime/english.sutime.txt");
        AnnotationPipeline pipeline = new AnnotationPipeline();
        pipeline.addAnnotator(new TokenizerAnnotator(false));
        pipeline.addAnnotator(new WordsToSentencesAnnotator(false));
        pipeline.addAnnotator(new POSTaggerAnnotator(false));
        pipeline.addAnnotator(new TimeAnnotator("sutime", props));

        Annotation annotation = new Annotation(ss);
        annotation.set(CoreAnnotations.DocDateAnnotation.class, "2016-10-10");
        pipeline.annotate(annotation);
        System.out.println(annotation.get(CoreAnnotations.TextAnnotation.class));
        List<CoreMap> timexAnnsAll = annotation.get(TimeAnnotations.TimexAnnotations.class);
        for (CoreMap cm : timexAnnsAll) {
            List<CoreLabel> tokens = cm.get(CoreAnnotations.TokensAnnotation.class);
            System.out.println(cm + " [from char offset " +
                    tokens.get(0).get(CoreAnnotations.CharacterOffsetBeginAnnotation.class) +
                    " to " + tokens.get(tokens.size() - 1).get(CoreAnnotations.CharacterOffsetEndAnnotation.class) + ']' +
                    " --> " + cm.get(TimeExpression.Annotation.class).getTemporal());
        }

    }

}