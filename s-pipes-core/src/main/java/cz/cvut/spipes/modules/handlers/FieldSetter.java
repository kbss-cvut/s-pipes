package cz.cvut.spipes.modules.handlers;

import java.lang.reflect.Field;

public class FieldSetter extends Setter {


    public FieldSetter(Field f, Object bean) {
        super(f, bean);
    }

    public void addValue(Object value) {
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
