package cz.cvut.spipes.modules.handlers;

import java.lang.reflect.Field;

public class FieldSetter implements Setter {

    private final Field f;
    private final Object bean;

    public FieldSetter(Field f, Object bean) {
        this.f = f;
        this.bean = bean;
    }

    public void addValue(Object value) {
        try {
            f.set(bean,value);
        } catch (IllegalAccessException ex) {
            // try again
            f.setAccessible(true);
            try {
                f.set(bean,value);
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            }
        }
    }


}
