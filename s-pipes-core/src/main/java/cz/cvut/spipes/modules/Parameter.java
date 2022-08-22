package cz.cvut.spipes.modules;

import cz.cvut.spipes.constants.KBSS_MODULE;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Parameter {
    String urlPrefix() default KBSS_MODULE.uri;
    String name();
}
