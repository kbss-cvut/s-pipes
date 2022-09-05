package cz.cvut.spipes.modules.model;

import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.spipes.constants.CSVW;
import cz.cvut.spipes.constants.KBSS_CSVW;
import cz.cvut.spipes.modules.exception.NoMatchException;

import java.util.function.UnaryOperator;

/**
 * Part of {@link TableSchema}, each column can have different metadata.
 */
@OWLClass(iri = CSVW.ColumnUri)
public class Column extends AbstractEntity {

    public Column() {}

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        setColumnVariable(this.name, name, value -> this.name = value, "name");
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
        setColumnVariable(this.aboutUrl, aboutUrl, value -> this.aboutUrl = value, "aboutUrl");
    }

    public String getPropertyUrl() {
        return propertyUrl;
    }

    public void setPropertyUrl(String propertyUrl) {
        setColumnVariable(this.propertyUrl, propertyUrl, value -> this.propertyUrl = value, "propertyUrl");
    }

    public String getValueUrl() {
        return valueUrl;
    }

    public void setValueUrl(String valueUrl) {
        setColumnVariable(this.valueUrl, valueUrl, value -> this.valueUrl = value, "valueUrl");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        setColumnVariable(this.title, title, value -> this.title = value, "title");
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

    public void setProperty(String property) {
        setColumnVariable(this.property, property, value -> this.property = value, "property");
    }

    private void setColumnVariable(String variable, String variable2, UnaryOperator<String> variableSetter, String variableName) {
        if (variable != null){
            checkVariable(variable, variable2, variableName);
        }else {
            variableSetter.apply(variable2);
        }
    }

    private void checkVariable(String variable, String variable2, String variableName) {
        if (!variable.equals(variable2)) {
            throw new NoMatchException(
                    String.format("Schema field '%s' with value '%s' does not match value '%s' from the input data ",
                            variableName, variable, variable2));
        }
    }
}
