package cz.cvut.spipes.modules.model;

import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.spipes.constants.CSVW;
import cz.cvut.spipes.constants.KBSS_CSVW;
import cz.cvut.spipes.modules.util.TabularModuleUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * A column represents a vertical arrangement of cells within a table.
 * The columns are used to process/validate input data
 * and are also part of {@link TableSchema}.
 * In case the column is under-specified,
 * the missing parts of the column are inferred from the data.
 * <p>
 * Thus, object provides setters that extend column,
 * moreover if input table schema is specified
 * the setters also checks the consistency with the table schema.
 * If setters are used in inconsistent way,
 * appropriate error is provided.
 */
@OWLClass(iri = CSVW.ColumnUri)
public class Column extends AbstractEntity {

    public Column() {}

    public Column(String name) {
        this.name = name;
    }

    public Column(String name, String title) {
        this.name = name;
        this.title = title;
    }

    @OWLAnnotationProperty(iri = CSVW.nameUri)
    private String name;

    @OWLAnnotationProperty(iri = CSVW.titleUri)
    private String title;

    @OWLAnnotationProperty(iri = KBSS_CSVW.propertyUri)
    private String property;

    @OWLAnnotationProperty(iri = CSVW.requiredUri)
    private Boolean required;

    @OWLAnnotationProperty(iri = CSVW.suppressOutputUri)
    private Boolean suppressOutput;

    @OWLAnnotationProperty(iri = CSVW.aboutUrlUri)
    private String aboutUrl;

    @OWLDataProperty(iri = CSVW.propertyUrlUri, datatype = "http://www.w3.org/2001/XMLSchema#string")
    private String propertyUrl;

    @OWLAnnotationProperty(iri = CSVW.valueUrlUri)
    private String valueUrl;

    private final transient TabularModuleUtils tabularModuleUtils = new TabularModuleUtils();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        tabularModuleUtils.setVariable(this.name, name, value -> this.name = value, "name");
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isSuppressOutput() {
        return suppressOutput;
    }

    public void setSuppressOutput(boolean suppressOutput) {
        this.suppressOutput = suppressOutput;
    }

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
        tabularModuleUtils.setVariable(this.propertyUrl, propertyUrl, value -> this.propertyUrl = value, "propertyUrl");
    }

    public String getValueUrl() {
        return valueUrl;
    }

    public void setValueUrl(String valueUrl) {
        tabularModuleUtils.setVariable(this.valueUrl, valueUrl, value -> this.valueUrl = value, "valueUrl");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        tabularModuleUtils.setVariable(this.title, title, value -> this.title = value, "title");
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Boolean getSuppressOutput() {
        return suppressOutput;
    }

    public void setSuppressOutput(Boolean suppressOutput) {
        this.suppressOutput = suppressOutput;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String dataPrefix, String sourceResourceUri, Column schemaColumn) throws UnsupportedEncodingException {
        String propertyValue = getPropertyUrl(dataPrefix, sourceResourceUri, schemaColumn);
        tabularModuleUtils.setVariable(this.property, propertyValue, value -> this.property = value, "property");
        tabularModuleUtils.setVariable(this.propertyUrl, propertyValue, value -> this.propertyUrl = value, "propertyUrl");
    }

    public void setProperty(String property){
        this.property = property;
        this.propertyUrl = property;
    }

    private String getPropertyUrl(String dataPrefix, String sourceResourceUri, Column schemaColumn)
            throws UnsupportedEncodingException {
        if (schemaColumn != null){
            if (schemaColumn.getPropertyUrl() != null){
                return schemaColumn.getPropertyUrl();
            }
            if (schemaColumn.getProperty() != null){
                return schemaColumn.getProperty();
            }
        }
        if (dataPrefix != null && !dataPrefix.isEmpty()) {
            return dataPrefix + URLEncoder.encode(name, StandardCharsets.UTF_8);
        }
        return sourceResourceUri + "#" + URLEncoder.encode(name, StandardCharsets.UTF_8);
    }
}
