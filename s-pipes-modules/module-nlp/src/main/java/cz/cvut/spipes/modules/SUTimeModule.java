package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.engine.ExecutionContextFactory;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import cz.cvut.spipes.sutime.AnnforModel;
import cz.cvut.spipes.sutime.DescriptorModel;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.TimeAnnotator;
import edu.stanford.nlp.time.TimeExpression;
import edu.stanford.nlp.util.CoreMap;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;


@SPipesModule(label = "temporal v0.1", comment = "Annotate temporal expressions in literals in input model.")
public class SUTimeModule extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(SUTimeModule.class);

    public static final String TYPE_URI = KBSS_MODULE.uri + "temporal-v0.1";

    @Parameter(urlPrefix = DescriptorModel.prefix, name = "has-rule-file", comment = "Rule file, multivalued.") // TODO - revise comment
    private List<Path> ruleFilePaths = new LinkedList<>();

    @Parameter(urlPrefix = DescriptorModel.prefix, name = "has-document-date", comment = "Document date format.") // TODO - revise comment
    private String documentDate; // TODO support other formats ?


    public SUTimeModule() {
    }

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

    @Override
    public void loadConfiguration() {

        if (this.resource.getProperty(DescriptorModel.has_document_date) != null) { // TODO set current date if not specified
            documentDate = getEffectiveValue(DescriptorModel.has_document_date).asLiteral().toString();
        }

        if (this.resource.getProperty(DescriptorModel.has_rule_file) != null) { //TODO support more rule files
            ruleFilePaths.add(Paths.get(getEffectiveValue(DescriptorModel.has_rule_file).asLiteral().toString()));
        }
    }

    @Override

    ExecutionContext executeSelf() {
        Model inputModel = executionContext.getDefaultModel();

        return ExecutionContextFactory.createContext(analyzeModel(inputModel));
    }

    private Model analyzeModel(Model m) {



        AnnotationPipeline pipeline = loadPipeline();


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
                    for(AnnforModel s:singleStDates){

                        Model  mm = ModelFactory.createDefaultModel();

                        Literal beginLiteral = mm.createTypedLiteral(s.getDateBegin());
                        Literal endLiteral = mm.createTypedLiteral(s.getDateEnd());
                        reifiedSt.addProperty(RDF.type, DescriptorModel.sutime_extraction);

                        reifiedSt.addProperty(DescriptorModel.extracted, s.getDateExtracted());
                        reifiedSt.addProperty(DescriptorModel.beginDate, beginLiteral);
                        reifiedSt.addProperty(DescriptorModel.endDate, endLiteral);
                        reifiedSt.addProperty(DescriptorModel.type, s.getDateType());

                        temporalAnnotationStmts.add(reifiedSt);
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }

            });

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

        ArrayList<AnnforModel> afmArr = new ArrayList<>();

        List<CoreMap> timexAnnsAll = annotation.get(TimeAnnotations.TimexAnnotations.class);
        if (!timexAnnsAll.isEmpty()) {

            for (CoreMap cm : timexAnnsAll) {
                List<CoreLabel> tokens = cm.get(CoreAnnotations.TokensAnnotation.class);

                edu.stanford.nlp.time.SUTime.Temporal temporal = cm.get(TimeExpression.Annotation.class).getTemporal();

                if (!temporal.getTimexType().toString().equals("DURATION") && !temporal.getTimexType().toString().equals("SET")) {
                    String extractionDateforModel = cm.toString();

                    edu.stanford.nlp.time.SUTime.Time suTimeBegin;
                    edu.stanford.nlp.time.SUTime.Time suTimeEnd;
                    String typeDateforModel = temporal.getTimexType().toString();

                    Calendar beginDateforModel = GregorianCalendar.getInstance();
                    Calendar endDateforModel = GregorianCalendar.getInstance();
                    Date date;
                    AnnforModel afm = null;

                    try {
                        suTimeBegin = temporal.getRange().beginTime();
                        suTimeEnd = temporal.getRange().endTime();

                        if ((!suTimeBegin.toString().equals("PRESENT_REF")) && (!suTimeBegin.toString().contains("X")) && (!suTimeBegin.toString().contains("UNKNOWN")) && (!suTimeBegin.toString().contains("REF")) && (!suTimeBegin.toString().contains("x")) && (suTimeBegin != null)) {

                            if (sdf.parse(suTimeBegin.toString()) != null) {
                                date = sdf.parse(suTimeBegin.toString());
                                beginDateforModel.setTime(date);
                            }
                        }
                        if ((!suTimeEnd.toString().equals("PRESENT_REF")) && (!suTimeEnd.toString().contains("X")) && (!suTimeEnd.toString().contains("UNKNOWN")) && (!suTimeEnd.toString().contains("REF")) && (!suTimeEnd.toString().contains("x")) && (suTimeEnd != null)) {

                            if (sdf.parse(suTimeEnd.toString()) != null) {
                                date = sdf.parse(suTimeEnd.toString());
                                endDateforModel.setTime(date);

                            }
                        }

                        afm = new AnnforModel(beginDateforModel, endDateforModel, typeDateforModel, extractionDateforModel);

                    } catch (NullPointerException e) {
                        LOG.info("catched in temporalAnalyze " + e.getMessage());

                    } catch (ParseException e) {
                        LOG.info("catched in parse exception " + e.getMessage());
                    }
                    afmArr = new ArrayList<>();
                    afmArr.add(afm);
                }
            }
        }

        return afmArr;

    }

    private AnnotationPipeline loadPipeline() {
        Properties props = new Properties();
        props.setProperty("sutime.includeRange", "true");
        props.setProperty("sutime.rules", "sutime/defs.txt, sutime/defs.sutime.txt, sutime/english.holidays.sutime.txt, sutime/english.sutime.txt");
        AnnotationPipeline pipeline = new AnnotationPipeline();
        pipeline.addAnnotator(new TokenizerAnnotator(false));
        pipeline.addAnnotator(new WordsToSentencesAnnotator(false));
        pipeline.addAnnotator(new POSTaggerAnnotator(false));
        pipeline.addAnnotator(new TimeAnnotator("sutime", props));
        return pipeline;
    }




}
