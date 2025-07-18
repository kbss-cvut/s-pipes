package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import cz.cvut.spipes.sutime.AnnforModel;
import cz.cvut.spipes.sutime.DescriptorModel;
import cz.cvut.spipes.util.JenaUtils;
import cz.cvut.spipes.util.QueryUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.TimeAnnotator;
import edu.stanford.nlp.time.TimeExpression;
import edu.stanford.nlp.util.CoreMap;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import cz.cvut.spipes.spin.model.Construct;
import cz.cvut.spipes.spin.vocabulary.SP;

import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Getter
@Setter
@SPipesModule(label = "temporal-v1", comment = "Module annotates input triples using NLP analysis of time using library SUTime.")
public class SUTimeModuleNew extends AnnotatedAbstractModule {

    private static final String TYPE_URI = KBSS_MODULE.uri + "temporal-v1";
    private static final String TYPE_PREFIX = TYPE_URI + "/";
    private static final int DEFAULT_PAGE_SIZE = 10000;
    private static final String LIMIT_OFFSET_CLAUSE_MARKER_NAME = "LIMIT_OFFSET";
    private static final Property P_PAGE_SIZE = ResourceFactory.createProperty(TYPE_PREFIX + "page-size");

    @Parameter(iri = TYPE_PREFIX + "page-size", comment = "Page size. Default value is 10000.")
    private int pageSize = DEFAULT_PAGE_SIZE;

    @Parameter(iri = SML.constructQuery,
            comment = "List of construct queries. The module annotates the lexical form of objects of the output statements of these queries.")// TODO - revise comment
    private List<Resource> constructQueries;

    @Parameter(iri = SML.replace, comment = "Replace context flag. Default value is false." )
    private boolean isReplace = false;

    @Parameter(iri = KBSS_MODULE.is_parse_text,
        comment = "Whether the query should be taken from sp:text property instead of from SPIN serialization," +
            " default is true.")
    private boolean parseText = true;

    @Parameter(iri = DescriptorModel.has_rule_file, comment = "Rule file, multivalued.")// TODO - review comment
    private List<Path> ruleFilePaths = new LinkedList<>();

    @Parameter(iri = DescriptorModel.has_document_date, comment = "Document date format.")// TODO - review comment
    private String documentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // TODO support other formats ?
    private AnnotationPipeline pipeline;

    public SUTimeModuleNew() {
        pipeline = loadPipeline();
    }

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

    @Override
    public ExecutionContext executeSelf() {

        Model defaultModel = executionContext.getDefaultModel();

        QuerySolution bindings = executionContext.getVariablesBinding().asQuerySolution();

        int count = 0;

        Model inferredModel = ModelFactory.createDefaultModel();
        Model previousInferredModel = ModelFactory.createDefaultModel();

        boolean queriedModelIsEmpty = false;

        while (!queriedModelIsEmpty) {

            count++;
            Model inferredInSingleIterationModel = ModelFactory.createDefaultModel();

            queriedModelIsEmpty = true;

            for (Resource constructQueryRes : constructQueries) {
                Construct spinConstructRes = constructQueryRes.as(Construct.class);

                Query query;
                if (parseText) {
                    String queryStr = spinConstructRes.getProperty(SP.text).getLiteral().getString();
                    query = QueryFactory.create(substituteQueryMarkers(count, queryStr));
                } else {
                    query = QueryUtils.createQuery(spinConstructRes);
                }

                Model queriedModel = QueryUtils.execConstruct(
                    query,
                    defaultModel,
                    bindings
                );

                if (queriedModel.size() > 0) {
                    queriedModelIsEmpty = false;
                }

                Model constructedModel = analyzeModel(queriedModel);

                inferredInSingleIterationModel = JenaUtils.createUnion(inferredInSingleIterationModel, constructedModel);
            }

            previousInferredModel = inferredModel;
            inferredModel = JenaUtils.createUnion(inferredModel, inferredInSingleIterationModel);
        }

        return createOutputContext(isReplace, inferredModel);
    }

    private Model analyzeModel(Model m) {

        log.debug("Extracting temporal information from model of size {}", m.size());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        List<Resource> temporalAnnotationStmts = new LinkedList<>();
        m.listStatements()
            .filterDrop(st -> !st.getObject().isLiteral())
            .forEachRemaining(
                st -> {
                    String objectStr = st.getObject().asLiteral().getLexicalForm();

                    try {
                        ArrayList<AnnforModel> singleStDates = temporalAnalysis(pipeline, objectStr);

                        if (!singleStDates.isEmpty()) {
                            Model mm = ModelFactory.createDefaultModel();
                            Resource reifiedSt = JenaUtils.addReifiedStatement(st);

                            for (AnnforModel s : singleStDates) {

                                Literal beginLiteral = mm.createTypedLiteral(sdf.format(s.getDateBegin().getTime()));
                                Literal endLiteral = mm.createTypedLiteral(sdf.format(s.getDateEnd().getTime()));
                                reifiedSt.addProperty(RDF.type, DescriptorModel.JENA.sutime_extraction);

                                reifiedSt.addProperty(DescriptorModel.JENA.extracted, s.getDateExtracted());
                                reifiedSt.addProperty(DescriptorModel.JENA.beginDate, beginLiteral);
                                reifiedSt.addProperty(DescriptorModel.JENA.endDate, endLiteral);
                                reifiedSt.addProperty(DescriptorModel.JENA.type, s.getDateType());

                                temporalAnnotationStmts.add(reifiedSt);
                            }

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

                    Calendar beginDateforModel = Calendar.getInstance();
                    Calendar endDateforModel = Calendar.getInstance();
                    Date date;
                    AnnforModel afm = null;

                    try {
                        if(((temporal.getRange().beginTime().getTimexValue()) != null) || ((temporal.getRange().endTime().getTimexValue()) != null)) {
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
                        }

                        afm = new AnnforModel(beginDateforModel, endDateforModel, typeDateforModel, extractionDateforModel);

                    } catch (NullPointerException e) {
                        log.info("catched in temporalAnalyze " + e.getMessage());

                    } catch (ParseException e) {
                        log.info("catched in parse exception " + e.getMessage());
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

    private String substituteQueryMarkers(int currentIteration, String queryStr) {
        int offset = pageSize * (currentIteration - 1);

        log.debug("Creating query with LIMIT {} for OFFSET {}.", pageSize, offset);
        String markerValue = "\n" + "OFFSET " + offset
            + "\n" + "LIMIT " + pageSize + "\n";

        return QueryUtils
            .substituteMarkers(LIMIT_OFFSET_CLAUSE_MARKER_NAME, markerValue, queryStr);
    }

}