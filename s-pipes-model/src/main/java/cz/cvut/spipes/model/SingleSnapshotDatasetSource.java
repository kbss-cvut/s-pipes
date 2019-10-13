
package cz.cvut.spipes.model;

import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.jopa.vocabulary.DC;
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import cz.cvut.spipes.Vocabulary;

import java.util.Map;
import java.util.Set;


/**
 * This class was generated by the OWL2Java tool version $VERSION$
 * 
 */
@OWLClass(iri = Vocabulary.s_c_single_snapshot_dataset_source)
public class SingleSnapshotDatasetSource
    extends DatasetSource
{

    @OWLAnnotationProperty(iri = RDFS.LABEL)
    protected String name;
    @OWLAnnotationProperty(iri = DC.Elements.DESCRIPTION)
    protected String description;
    @Types
    protected Set<String> types;
    @Id(generated = true)
    protected String id;
    @Properties
    protected Map<String, Set<Object>> properties;
    @OWLObjectProperty(iri = Vocabulary.s_p_inv_dot_has_source)
    @ParticipationConstraints({
        @ParticipationConstraint(owlObjectIRI = Vocabulary.s_c_dataset_publication, max = 1)
    })
    protected Set<Description> inv_dot_has_source;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setTypes(Set<String> types) {
        this.types = types;
    }

    public Set<String> getTypes() {
        return types;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setProperties(Map<String, Set<Object>> properties) {
        this.properties = properties;
    }

    public Map<String, Set<Object>> getProperties() {
        return properties;
    }

    public void setInv_dot_has_source(Set<Description> inv_dot_has_source) {
        this.inv_dot_has_source = inv_dot_has_source;
    }

    public Set<Description> getInv_dot_has_source() {
        return inv_dot_has_source;
    }

}
