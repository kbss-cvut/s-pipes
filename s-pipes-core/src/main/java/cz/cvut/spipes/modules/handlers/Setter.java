package cz.cvut.spipes.modules.handlers;

import java.lang.reflect.Field;

/**
 * An abstract class representing a setter that allows adding a value of type {@code T}
 * to a field of an object (bean).
 *
 * <p>This class is intended to be extended by specific implementations that provide
 * the logic for adding a value to the field.
 *
 * @param <T> the type of value to be added to the field
 */
public abstract class Setter<T>{

    /**
     * The field to which the value will be set.
     */
    protected final Field f;

    /**
     * The object (bean) containing the field.
     */
    protected final Object bean;

    protected Setter(Field f, Object bean) {
        this.f = f;
        this.bean = bean;
    }

    /**
     * Adds the specified value to the field.
     *
     * @param value the value to be added to the field
     */
    abstract void addValue(T value);

    Field getField(){
        return f;
    }
    Object getBean(){
        return bean;
    }
}
