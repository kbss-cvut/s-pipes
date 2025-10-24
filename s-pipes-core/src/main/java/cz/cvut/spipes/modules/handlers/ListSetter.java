package cz.cvut.spipes.modules.handlers;

import java.lang.reflect.Field;
import java.util.List;

public class ListSetter extends Setter<Object>{

    public ListSetter(Field f, Object bean) {
        super(f, bean);
    }

    @Override
    public void setValue(Object value) {
        try {
            List<Object> list = (List<Object>) f.get(bean);

            if (list == null) {
                list = new java.util.ArrayList<>();
                f.set(bean, list);
            }else if(!list.isEmpty()){
                list.clear();
            }

            if (value instanceof List) {
                list.addAll((List<?>) value);
            } else {
                list.add(value);
            }

        } catch (IllegalAccessException ex) {
            f.setAccessible(true);
            try {
                List<Object> list = (List<Object>) f.get(bean);

                if (list == null) {
                    list = new java.util.ArrayList<>();
                    f.set(bean, list);
                }else if(!list.isEmpty()){
                    list.clear();
                }

                if (value instanceof List) {
                    list.addAll((List<?>) value);
                } else {
                    list.add(value);
                }

            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            }
        }
    }
}
