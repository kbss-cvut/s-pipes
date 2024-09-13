package cz.cvut.spipes.sutime;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import static org.apache.jena.rdf.model.ResourceFactory.createProperty;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;

public class DescriptorModel {

    /**
     * The namespace of the vocabulary as a string
     */
    public static final String prefix = "http://onto.fel.cvut.cz/ontologies/dataset-descriptor/temporal-v1/";

    public static final String sutime_extraction = prefix + "sutime-extraction";

    public static final String has_document_date = prefix + "has-document-date";
    public static final String has_rule_file = prefix + "has-rule-file";

    public static final String beginDate = prefix + "beginDate";
    public static final String endDate = prefix + "endDate";
    public static final String type = prefix + "type";
    public static final String extracted = prefix + "extracted";

    public static class JENA {
        public static final Resource sutime_extraction = createResource(DescriptorModel.sutime_extraction);

        public static final Property has_document_date = createProperty(DescriptorModel.has_document_date);
        public static final Property has_rule_file = createProperty(DescriptorModel.has_rule_file);

        public static final Property beginDate = createProperty(DescriptorModel.beginDate);
        public static final Property endDate = createProperty(DescriptorModel.endDate);
        public static final Property type = createProperty(DescriptorModel.type);
        public static final Property extracted = createProperty(DescriptorModel.extracted);
    }
}