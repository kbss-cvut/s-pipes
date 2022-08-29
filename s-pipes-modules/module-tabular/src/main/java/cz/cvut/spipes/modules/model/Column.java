package cz.cvut.spipes.modules.model;

import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.spipes.constants.CSVW;
import cz.cvut.spipes.constants.KBSS_CSVW;

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
        this.name = name;
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
        this.aboutUrl = aboutUrl;
    }

    public String getPropertyUrl() {
        return propertyUrl;
    }

    public void setPropertyUrl(String propertyUrl) {
        this.propertyUrl = propertyUrl;
    }

    public String getValueUrl() {
        return valueUrl;
    }

    public void setValueUrl(String valueUrl) {
        this.valueUrl = valueUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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
        this.property = property;
    }
}
