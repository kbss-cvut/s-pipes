package cz.cvut.spipes.debug.model;

import java.io.Serializable;
import java.util.Set;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.Types;
import cz.cvut.kbss.jopa.model.annotations.util.NonEntity;

@NonEntity
public class AbstractDto implements Serializable {

    @Id(generated = true)
    protected String id;

    @Types
    protected Set<String> types;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<String> getTypes() {
        return types;
    }

    public void setTypes(Set<String> types) {
        this.types = types;
    }
}
