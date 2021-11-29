package cz.cvut.spipes.modules.model;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLAnnotationProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.spipes.constants.CSVW;

/**
 * Part of {@link TableSchema}, each column can have different metadata.
 */
@OWLClass(iri = CSVW.ColumnUri)
public class Column {

    @Id(generated = true)
    private String id;

    @OWLAnnotationProperty(iri = CSVW.nameUri)
    private String name;

    @OWLAnnotationProperty(iri = CSVW.requiredUri)
    private boolean required;

    @OWLAnnotationProperty(iri = CSVW.suppressOutputUri)
    private boolean suppressOutput;

    @OWLAnnotationProperty(iri = CSVW.aboutUrlUri)
    private String aboutUrl;

    @OWLAnnotationProperty(iri = CSVW.propertyUrlUri)
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
}
