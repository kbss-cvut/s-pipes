package cz.cvut.spipes.modules.handlers;

import java.lang.reflect.Field;

public abstract class Setter<T>{

    protected final Field f;
    protected final Object bean;

    protected Setter(Field f, Object bean) {
        this.f = f;
        this.bean = bean;
    }

    abstract void addValue(T value);

    Field getField(){
        return f;
    }
    Object getBean(){
        return bean;
    }
}
