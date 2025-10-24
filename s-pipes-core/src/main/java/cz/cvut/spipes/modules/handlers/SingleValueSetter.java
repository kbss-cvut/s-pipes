package cz.cvut.spipes.modules.handlers;

import java.lang.reflect.Field;

public class SingleValueSetter<T> extends Setter<T> {


    public SingleValueSetter(Field f, Object bean) {
        super(f, bean);
    }

    public void setValue(T value) {
        try {
            f.set(bean,value);
        } catch (IllegalAccessException ex) {
            f.setAccessible(true);
            try {
                f.set(bean,value);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            }
        }
    }

}
