package cz.cvut.sempipes.modules;

import com.opencsv.CSVWriter;
import com.opencsv.bean.*;
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
import edu.stanford.nlp.time.SUTime.Temporal;
import edu.stanford.nlp.util.CoreMap;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.eclipse.rdf4j.query.algebra.Str;
import org.joda.time.DateTime;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.time.*;

import static org.springframework.util.StringUtils.hasText;

public class AnalyzeTemporalModule extends AbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(AnalyzeTemporalModule.class);

    //private static final Logger LOG = LoggerFactory.getLogger(MySUTimeModule.class);

    public static final String TYPE_URI = KBSS_MODULE.getURI() + "analyze/";


    //    final Property HAS_DATE = ResourceFactory.createProperty("http://kbss/has-date");
    private String documentDate;


    String sparqlEndpoint;
    String datasetNamedGraph;
    String query;

    List<List<String>> AnnotationsArr = new ArrayList<>();
    List<String[]> AnnAll = new ArrayList<String[]>();

    public AnalyzeTemporalModule() {
    }

    @Override
    public String getTypeURI() {
        return KBSS_MODULE.getURI() + "AnalyzeTemporal";
    }

    @Override
    public void loadConfiguration() {
    }


    public String getDatasetNamedGraph() {
        return datasetNamedGraph;
    }

    public void setDatasetNamedGraph(String datasetNamedGraph) {
        this.datasetNamedGraph = datasetNamedGraph;
    }


    public String getq() {
        return query;
    }

    public void setq(String query) {
        this.query = query;
    }

    public String getSparqlEndpoint() {
        return sparqlEndpoint;
    }

    public void setSparqlEndpoint(String sparqlEndpoint) {
        this.sparqlEndpoint = sparqlEndpoint;
    }


    @Override
    ExecutionContext executeSelf() {
        //Model defaultModel = executionContext.getDefaultModel();
        ArrayList<SUTime.Time> allDates = new ArrayList<>();



        QueryExecution qexec = QueryExecutionFactory.sparqlService(this.sparqlEndpoint, this.query);
        Model m = qexec.execConstruct();
        Model outputModel = ModelFactory.createDefaultModel();

        String[] headers = new String[9];
        headers[0] = "time expression";
        headers[1] = "temporal value";
        headers[2] = "timex value";
        headers[3] = "timex Type";
        headers[4] = "begin Date";
        headers[5] = "end Date";
        headers[6] = "Start offset";
        headers[7] = "End Offset";
        headers[8] = "Text value";
        AnnAll.add(headers);



       //allDates = analyzeModel(m);
        outputModel = analyzeModel(m);
//       Collections.sort(allDates);
//        try {
//            writeDatesToFile(allDates, "./datesFile");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


        executionContext.getDefaultModel().add(m);

        //return executionContext;
        return ExecutionContextFactory.createContext(outputModel);

    }


    public AnnotationPipeline loadPipeline() {
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

    public Model analyzeModel(Model m) {
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
                            ArrayList<AnnforModel> singleStDates = temporalAnalysis(objectStr);
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
        try {
            writeDatesToFile(allStsDates, "./datesFile");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Model outputModel = ModelFactory.createDefaultModel();
        temporalAnnotationStmts.forEach(
                st -> outputModel.add(st.listProperties())
        );
        return outputModel;


    }

    public ArrayList<AnnforModel> temporalAnalysis(String s) throws IOException {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        ArrayList<SUTime.Time>  allDatesArr = new ArrayList<>();
        ArrayList<AnnforModel> afmArr = new ArrayList<>();

        AnnotationPipeline pipeline = loadPipeline();

        Annotation annotation = new Annotation(s);
        annotation.set(CoreAnnotations.DocDateAnnotation.class, sdf.format(cal.getTime()));
        pipeline.annotate(annotation);

        List<CoreMap> timexAnnsAll = annotation.get(TimeAnnotations.TimexAnnotations.class);
        if (!timexAnnsAll.isEmpty()) {
            System.out.println(annotation.get(CoreAnnotations.TextAnnotation.class));

            for (CoreMap cm : timexAnnsAll) {
                List<CoreLabel> tokens = cm.get(CoreAnnotations.TokensAnnotation.class);

                String startOffset = tokens.get(0).get(CoreAnnotations.CharacterOffsetBeginAnnotation.class).toString();

                String endOffset = tokens.get(tokens.size() - 1).get(CoreAnnotations.CharacterOffsetEndAnnotation.class).toString();

                //System.out.println("----------------------");
                Temporal temporal = cm.get(TimeExpression.Annotation.class).getTemporal();
                String extractionDateforModel = cm.toString();

                SUTime.Time beginTime;
                SUTime.Time endTime;
                String typeDateforModel = temporal.getTimexType().toString();
                String [] singleAnnotationsArr = new String[9];
                singleAnnotationsArr[0]= cm.toString();
                singleAnnotationsArr[1]= temporal.toString();
                singleAnnotationsArr[2]= temporal.getTimexValue();
                singleAnnotationsArr[3]= typeDateforModel;

                String beginDateforModel = "";
                String endDateforModel = "";
                AnnforModel afm = new AnnforModel("","", "", "");

                try {
                   beginTime = temporal.getRange().beginTime();
                   endTime = temporal.getRange().endTime();
                   if ((!beginTime.toString().equals("PRESENT_REF")) && (!beginTime.toString().contains("X")) && (!beginTime.toString().contains("UNKNOWN")) && (!beginTime.toString().contains("REF")) && (!beginTime.toString().contains("x"))){
                       allDatesArr.add(beginTime);
                       beginDateforModel = beginTime.toString();
                   }
                    if ((!endTime.toString().equals("PRESENT_REF")) && (!endTime.toString().contains("X")) && (!endTime.toString().contains("UNKNOWN")) && (!endTime.toString().contains("REF")) && (!endTime.toString().contains("x"))){
                        allDatesArr.add(endTime);
                        endDateforModel = endTime.toString();
                    }

                   singleAnnotationsArr[4] = beginTime.toString();
                   singleAnnotationsArr[5] = endTime.toString();

                   afm = new AnnforModel(beginDateforModel, endDateforModel, typeDateforModel, extractionDateforModel);

                } catch (NullPointerException e){

                }

                singleAnnotationsArr[6]= startOffset;
                singleAnnotationsArr[7]= endOffset;
                singleAnnotationsArr[8]= annotation.get(CoreAnnotations.TextAnnotation.class);
                AnnAll.add(singleAnnotationsArr);

                afmArr = new ArrayList<>();
                afmArr.add(afm);

            }

//            writeArrtoCSVFile(AnnAll,"./testtt.csv");

        }

        return afmArr;

    }

    public void writeArrtoCSVFile(List<String[]> arr, String file) throws IOException {
        CSVWriter csvWriter = new CSVWriter(new FileWriter(file), ',');
        csvWriter.writeAll(arr);
        csvWriter.close();
    }

    public void writeDatesToFile(ArrayList<LocalDate> arr, String file) throws IOException {
        FileWriter fileWriter = new FileWriter(file, true);
        //fileWriter.write(System.lineSeparator());
        for (LocalDate s: arr
             ) {
            fileWriter.write(s.toString());
            fileWriter.write("\n");
            }

        fileWriter.flush();
        fileWriter.close();
    }

    public ArrayList<Date> convertStringToDate(ArrayList<String> arr){
        ArrayList<Date> arr2 = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        for (String dateString : arr) {
            try {
                //if(){
                    arr2.add(simpleDateFormat.parse(dateString));
               // }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return arr2;
    }


    }
