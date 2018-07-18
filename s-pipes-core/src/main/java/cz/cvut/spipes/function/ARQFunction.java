package cz.cvut.spipes.function;

import org.apache.jena.sparql.function.Function;

//TODO unify mechanism of SM:functions, SPIN:functions and java based functions
public interface ARQFunction extends Function {
    String getTypeURI();
}
