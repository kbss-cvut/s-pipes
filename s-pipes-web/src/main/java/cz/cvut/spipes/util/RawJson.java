package cz.cvut.spipes.util;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

public class RawJson {

    private String value;

    public RawJson() {
    }

    public RawJson(String value) {
        this.value = value;
    }

    @JsonValue
    @JsonRawValue
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RawJson rawJson = (RawJson) o;

        return !(value != null ? !value.equals(rawJson.value) : rawJson.value != null);

    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}