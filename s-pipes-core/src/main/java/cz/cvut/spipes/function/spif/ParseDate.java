package cz.cvut.spipes.function.spif;

import cz.cvut.spipes.constants.SPIF;
import cz.cvut.spipes.function.ValueFunction;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.topbraid.spin.arq.AbstractFunction3;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ParseDate extends AbstractFunction3 implements ValueFunction {


    private static final String TYPE_IRI = SPIF.getURI() + "parseDate";

    @Override
    public String getTypeURI() {
        return TYPE_IRI;
    }

    @Override
    protected NodeValue exec(Node text, Node pattern, Node patternLanguage, FunctionEnv env) {


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern.getLiteralValue().toString());

        LocalDate date
            = LocalDate.parse(text.getLiteralValue().toString(), formatter);


        return null;
    }
}
