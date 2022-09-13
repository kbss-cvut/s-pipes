package cz.cvut.spipes.modules.model;

import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.spipes.constants.CSVW;
import cz.cvut.spipes.constants.KBSS_CSVW;
import cz.cvut.spipes.modules.util.TabularModuleUtils;
import cz.cvut.spipes.registry.StreamResource;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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

    public void setProperty(String dataPrefix, StreamResource sourceResource) throws UnsupportedEncodingException {
        String propertyValue = getPropertyUrl(dataPrefix, sourceResource);
        tabularModuleUtils.setVariable(this.property, propertyValue, value -> this.property = value, "property");
        tabularModuleUtils.setVariable(this.propertyUrl, propertyValue, value -> this.propertyUrl = value, "propertyUrl");
    }

    private String getPropertyUrl(String dataPrefix, StreamResource sourceResource)
            throws UnsupportedEncodingException {
        if (getPropertyUrl() != null) {
            return getPropertyUrl();
        }
        if (getProperty() != null) {
            return getProperty();
        }
        if (dataPrefix != null && !dataPrefix.isEmpty()) {
            return dataPrefix + URLEncoder.encode(name, "UTF-8");
        }
        return sourceResource.getUri() + "#" + URLEncoder.encode(name, "UTF-8");
    }

    public void setValueUrl(Model outputModel, String cellValue, String tableSchemaAboutUrl, int rowNumber) {
        String valueURL = null;
        if(getValueUrl() != null) valueURL = getValueUrl();

        if (valueURL != null && !valueURL.isEmpty()) {
            setValueUrl(valueURL);
        } else {
            if (cellValue != null) {
                Resource columnResource = ResourceFactory
                        .createResource(createAboutUrl(tableSchemaAboutUrl, rowNumber));

                outputModel.add(ResourceFactory.createStatement(
                        columnResource,
                        outputModel.createProperty(getPropertyUrl()),
                        ResourceFactory.createPlainLiteral(cellValue)));
            }
        }
    }

    public String createAboutUrl(String tableSchemaAboutUrl, int rowNumber) {
        String columnAboutUrlStr = tableSchemaAboutUrl;
        if (columnAboutUrlStr == null) columnAboutUrlStr = getAboutUrl();
        columnAboutUrlStr = columnAboutUrlStr.replace(
                "{_row}",
                Integer.toString(rowNumber + 1)
        );
        return columnAboutUrlStr;
    }
}
