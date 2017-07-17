package cz.cvut.sempipes.modules;

import cz.cvut.sempipes.constants.KBSS_MODULE;
import cz.cvut.sempipes.engine.ExecutionContext;
import cz.cvut.sempipes.engine.ExecutionContextFactory;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.time.SUTime;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.TimeAnnotator;
import edu.stanford.nlp.time.TimeExpression;
import edu.stanford.nlp.util.CoreMap;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Created by Miroslav Blasko on 10.10.16.
 */
public class SUTime2Module extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(SUTime2Module.class);

    public static final String TYPE_URI = KBSS_MODULE.getURI() + "su-time-v2";

    private List<Path> ruleFilePaths = new LinkedList<>();
    private String documentDate; // TODO support other formats ?


    public SUTime2Module() {
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
        Model inputModel = executionContext.getDefaultModel();

        return ExecutionContextFactory.createContext(analyzeModel(inputModel));
    }

    private Model analyzeModel(Model m) {



        AnnotationPipeline pipeline = loadPipeline();


        ArrayList<LocalDate> allStsDates = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<ReifiedStatement> temporalAnnotationStmts = new LinkedList<>();
        m.listStatements()
            .filterDrop(st -> !st.getObject().isLiteral())
            .toList().forEach(
            st -> {
                String objectStr = st.getObject().asLiteral().getLexicalForm();

                ReifiedStatement reifiedSt = m.createReifiedStatement(st);
                try {
                    ArrayList<AnnforModel> singleStDates = temporalAnalysis(pipeline, objectStr);
                    ArrayList<LocalDate> singleStAllDates = new ArrayList<>();
                    for(AnnforModel s:singleStDates){

                        String begin = s.getDateBegin();
                        String end = s.getDateEnd();
                        LocalDate beginLocalDate;
                        LocalDate endLocalDate;
                        try {
                            if (!begin.equals("")) {
                                beginLocalDate = LocalDate.parse(begin, formatter);
                                singleStAllDates.add(beginLocalDate);
                            }
                            if (!end.equals("")) {
                                endLocalDate = LocalDate.parse(end, formatter);
                                singleStAllDates.add(endLocalDate);
                            }
                        } catch (DateTimeParseException e){
                        }

                        reifiedSt.addProperty(RDF.type, MySUTime.sutime_extraction);

                        reifiedSt.addProperty(MySUTime.extracted, s.getDateExtracted());
                        reifiedSt.addProperty(MySUTime.beginDate, begin);
                        reifiedSt.addProperty(MySUTime.endDate, end);
                        reifiedSt.addProperty(MySUTime.type, s.getDateType());

                        temporalAnnotationStmts.add(reifiedSt);
                    }


                    allStsDates.addAll(singleStAllDates);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            });

        Collections.sort(allStsDates);
        LOG.trace("All extracted date : ", allStsDates);

        Model outputModel = ModelFactory.createDefaultModel();
        temporalAnnotationStmts.forEach(
            st -> outputModel.add(st.listProperties())
        );
        return outputModel;


    }

    private ArrayList<AnnforModel> temporalAnalysis(AnnotationPipeline pipeline, String s) throws IOException {

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Annotation annotation = new Annotation(s);
        annotation.set(CoreAnnotations.DocDateAnnotation.class, sdf.format(cal.getTime()));
        pipeline.annotate(annotation);

        ArrayList<SUTime.Time>  allDatesArr = new ArrayList<>();
        ArrayList<AnnforModel> afmArr = new ArrayList<>();
//

        List<CoreMap> timexAnnsAll = annotation.get(TimeAnnotations.TimexAnnotations.class);
        if (!timexAnnsAll.isEmpty()) {
            System.out.println(annotation.get(CoreAnnotations.TextAnnotation.class));

            for (CoreMap cm : timexAnnsAll) {
                List<CoreLabel> tokens = cm.get(CoreAnnotations.TokensAnnotation.class);

                String startOffset = tokens.get(0).get(CoreAnnotations.CharacterOffsetBeginAnnotation.class).toString();

                String endOffset = tokens.get(tokens.size() - 1).get(CoreAnnotations.CharacterOffsetEndAnnotation.class).toString();

                //System.out.println("----------------------");
                SUTime.Temporal temporal = cm.get(TimeExpression.Annotation.class).getTemporal();
                String extractionDateforModel = cm.toString();

                SUTime.Time beginTime;
                SUTime.Time endTime;
                String typeDateforModel = temporal.getTimexType().toString();

                String beginDateforModel = "";
                String endDateforModel = "";
                AnnforModel afm = new AnnforModel("", "", "", "");

                try {
                    beginTime = temporal.getRange().beginTime();
                    endTime = temporal.getRange().endTime();
                    if ((!beginTime.toString().equals("PRESENT_REF")) && (!beginTime.toString().contains("X")) && (!beginTime.toString().contains("UNKNOWN")) && (!beginTime.toString().contains("REF")) && (!beginTime.toString().contains("x"))) {
                        allDatesArr.add(beginTime);
                        beginDateforModel = beginTime.toString();
                    }
                    if ((!endTime.toString().equals("PRESENT_REF")) && (!endTime.toString().contains("X")) && (!endTime.toString().contains("UNKNOWN")) && (!endTime.toString().contains("REF")) && (!endTime.toString().contains("x"))) {
                        allDatesArr.add(endTime);
                        endDateforModel = endTime.toString();
                    }

                    afm = new AnnforModel(beginDateforModel, endDateforModel, typeDateforModel, extractionDateforModel);

                } catch (NullPointerException e) {

                }
                afmArr = new ArrayList<>();
                afmArr.add(afm);
            }
        }

        return afmArr;

    }

    private boolean containsString(Object time, String str) {
        if (time == null) {
            return false;
        }
        return ! time.toString().contains(str);
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
