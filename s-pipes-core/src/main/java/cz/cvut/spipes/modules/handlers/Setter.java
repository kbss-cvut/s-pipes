package cz.cvut.spipes.modules.handlers;

import java.lang.reflect.Field;

public interface Setter<T>{

    void addValue(T value);

    Field getField();
    Object getBean();
}
