package cz.cvut.spipes.modules;

import cz.cvut.spipes.modules.handlers.Handler;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Parameter {
    String iri();
    String comment() default "";
    Class<? extends Handler> handler() default Handler.class;
}
