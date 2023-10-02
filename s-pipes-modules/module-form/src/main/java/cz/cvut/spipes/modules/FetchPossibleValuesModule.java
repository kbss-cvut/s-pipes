package cz.cvut.spipes.modules;


import cz.cvut.sforms.SFormsVocabularyJena;
import cz.cvut.spipes.constants.KBSS_MODULE;
import cz.cvut.spipes.constants.SML;
import cz.cvut.spipes.engine.ExecutionContext;
import cz.cvut.spipes.form.JenaFormUtils;
import cz.cvut.spipes.modules.annotations.SPipesModule;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@SPipesModule(label = "fetch possible values", comment =
        "Fetches possible values for answers of questions. Inputs are forms using Q&A model. Possible values of " +
        "questions are added to questions that does not have any value attached and contains possible value query."
)
public class FetchPossibleValuesModule extends AnnotatedAbstractModule {

    private static final Logger LOG = LoggerFactory.getLogger(FetchPossibleValuesModule.class);
    private static final String TYPE_URI = KBSS_MODULE.uri + "fetch-possible-values";

    @Parameter(urlPrefix = SML.uri, name = "replace", comment = "Specifies whether a module should overwrite triples" +
        " from its predecessors. When set to true (default is false), it prevents" +
        " passing through triples from the predecessors.")
    private boolean isReplace = false;


    @Override
    ExecutionContext executeSelf() {

        Model inpModel = this.getExecutionContext().getDefaultModel();

        Model constructedModel = ModelFactory.createDefaultModel();

        Map<String, PossibleValuesQueryProcessor> query2possibleValue = new HashMap<>();

        JenaFormUtils.getQuestions(inpModel)
            .filterKeep(q -> q.hasProperty(SFormsVocabularyJena.s_p_has_possible_values_query))
            .filterKeep(q -> !q.hasProperty(SFormsVocabularyJena.s_p_has_possible_value))
            .forEachRemaining(
                q -> {

                    String possibleValuesQuery = getPossibleValuesQuery(q);
                    PossibleValuesQueryProcessor qProcessor = query2possibleValue.get(possibleValuesQuery);
                    if (qProcessor == null) {
                        qProcessor = new PossibleValuesQueryProcessor(possibleValuesQuery);
                        LOG.debug("Retrieved {} new possible values for question {}.",
                            qProcessor.getPossibleValuesCount(), q.getURI());
                        query2possibleValue.put(possibleValuesQuery, qProcessor);
                    } else {
                        LOG.debug("Using cache of {} possible values for question {}.",
                            qProcessor.getPossibleValuesCount(), q.getURI());
                    }
                    qProcessor.addQuestion(q);
                }
            );

        query2possibleValue.values().forEach(
            qP -> {
                constructedModel.add(qP.getPossibleValuesModel());
                qP.getRelatedQuestions().forEach(
                    q -> qP.getPossibleValueResources().forEach(
                        v -> constructedModel.add(q, SFormsVocabularyJena.s_p_has_possible_value, v)
                    )
                );
            }
        );

        return createOutputContext(isReplace, constructedModel);
    }

    public String getPossibleValuesQuery(Resource question) {
        return question
            .getRequiredProperty(SFormsVocabularyJena.s_p_has_possible_values_query)
            .getObject()
            .asLiteral()
            .toString();
    }

    public boolean isReplace() {
        return isReplace;
    }

    public void setReplace(boolean replace) {
        isReplace = replace;
    }

    @Override
    public String getTypeURI() {
        return TYPE_URI;
    }

}
