package cz.cvut.spipes.modules.model;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.spipes.config.ExecutionConfig;
import cz.cvut.spipes.constants.CSVW;
import cz.cvut.spipes.modules.exception.TableSchemaException;
import cz.cvut.spipes.modules.util.JopaPersistenceUtils;
import cz.cvut.spipes.modules.util.TabularModuleUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.ResourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Represents schema of tabular data (according to relevant W3C standard),
 * possibly specified partially. The schema is used to process/validate
 * input tabular data. In case the schema is under-specified,
 * the missing parts of the schema are inferred from the data.
 *
 * Thus, object provides setters to extend the tabular schema ONLY
 * in a consistent way. If setters are used in inconsistent way,
 * appropriate error is provided.
 *
 */
@OWLClass(iri = CSVW.TableSchemaUri)
public class TableSchema extends AbstractEntity {

    private static final Logger LOG = LoggerFactory.getLogger(TableSchema.class);

    @OWLDataProperty(iri = CSVW.aboutUrlUri, datatype = CSVW.uriTemplate)
    private String aboutUrl;

    @OWLAnnotationProperty(iri = CSVW.propertyUrlUri)
    private String propertyUrl;

    @OWLAnnotationProperty(iri = CSVW.valueUrlUri)
    private String valueUrl;

    @OWLObjectProperty(iri = CSVW.uri + "column", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Column> columnsSet = new HashSet<>();

    private final transient TabularModuleUtils tabularModuleUtils = new TabularModuleUtils();

    public String getAboutUrl() {
        return aboutUrl;
    }

    public void setAboutUrl(String aboutUrl) {
        tabularModuleUtils.setVariable(this.aboutUrl, aboutUrl, value -> this.aboutUrl = value, "aboutUrl");
    }

    public String getPropertyUrl() {
        return propertyUrl;
    }

    public void setPropertyUrl(String propertyUrl) {
        tabularModuleUtils
                .setVariable(this.propertyUrl, propertyUrl, value -> this.propertyUrl = value, "propertyUrl");
    }

    public String getValueUrl() {
        return valueUrl;
    }

    public void setValueUrl(String valueUrl) {
        tabularModuleUtils.setVariable(this.valueUrl, valueUrl, value -> this.valueUrl = value, "valueUrl");
    }

    public Set<Column> getColumnsSet() {
        return columnsSet;
    }

    public void setColumnsSet(Set<Column> columnsSet) {
        this.columnsSet = columnsSet;
    }


    public List<Column> sortColumns(List<String> orderList){

        if (orderList.isEmpty()) return new ArrayList<>(columnsSet);

        List<Column> columnList = new ArrayList<>(orderList.size());

        for (String uri : orderList) {
            Optional<Column> col = columnsSet.stream()
                    .filter(column -> column.getUri().toString().equals(uri))
                    .findFirst();
            col.ifPresent(columnList::add);
        }
        return columnList;
    }

    public void adjustProperties(boolean hasInputSchema, List<Column> outputColumns, String sourceResourceUri) {
        if (hasInputSchema){
            if(getColumnsSet().size() > outputColumns.size()) {
                throwExtraColumnsError(outputColumns);
            }
            setAboutUrl(sourceResourceUri + "#row-{_row}");
            getColumnsSet().forEach(column -> column.setUri(null));
            setUri(null);
        }else{
            setColumnsSet(new HashSet<>(outputColumns));
        }
    }

    private void throwExtraColumnsError(List<Column> outputColumns) {
        StringBuilder errorMessage =
            new StringBuilder("There is an additional column in retrieved input data schema compared to expected one");

        for (Column column : getColumnsSet()) {
            if (outputColumns.stream().noneMatch(outputColumn -> outputColumn.getName().equals(column.getName()))) {
                errorMessage
                        .append("\n")
                        .append(String.format("Column with name `%s` is extra.", column.getName()));
            }
        }
       logError(errorMessage.toString());
    }

    public void addColumnsList(EntityManager em, List<Column> outputColumns) {
        Model persistenceModel = JopaPersistenceUtils.getDataset(em).getDefaultModel();
        RDFNode[] elements = new RDFNode[outputColumns.size()];

        for (int i = 0; i < outputColumns.size(); i++) {
            Resource n = persistenceModel.getResource(outputColumns.get(i).getUri().toString());
            elements[i] = n;
        }

        RDFList columnList = persistenceModel.createList(elements);
        Resource convertedTableSchema =
                ResourceUtils.renameResource(persistenceModel.getResource(getUri().toString()), null);

        persistenceModel.add(
                convertedTableSchema,
                CSVW.columns,
                columnList);
    }

    public void setAboutUrl(Column column, String sourceResourceUri) {
        String columnAboutUrl = null;
        if(column.getAboutUrl() != null){
            columnAboutUrl = column.getAboutUrl();
        }

        if (columnAboutUrl != null && !columnAboutUrl.isEmpty()) {
            column.setAboutUrl(columnAboutUrl);
        } else {
            String tableSchemaAboutUrl = sourceResourceUri + "#row-{_row}";
            tabularModuleUtils.setVariable(aboutUrl, tableSchemaAboutUrl, value -> this.aboutUrl = value, "aboutUrl");
        }
    }

    public Column getColumn(String columnName) {
        for (Column column : getColumnsSet()) {
            if (column.getName() != null && column.getName().equals(columnName)) {
                return column;
            }
        }

        String errorMessage = String.format("There is missing column in retrieved input schema compared to expected one" +
                "\n Column `%s` does not exist in input schema.", columnName);
        logError(errorMessage);
        return null;
    }

    private void logError(String msg) {
        if (ExecutionConfig.isExitOnError()) {
            throw new TableSchemaException(msg);
        }else LOG.error(msg);
    }

    public String createAboutUrl(int rowNumber) {
        String columnAboutUrlStr = aboutUrl;
        if (columnAboutUrlStr == null) columnAboutUrlStr = getAboutUrl();
        columnAboutUrlStr = columnAboutUrlStr.replace(
                "{_row}",
                Integer.toString(rowNumber + 1)
        );
        return columnAboutUrlStr;
    }
}
