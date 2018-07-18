package cz.cvut.spipes.function;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.topbraid.spin.arq.AbstractFunction1;
import org.topbraid.spin.arq.AbstractFunction2;

public class MatchesPassword extends AbstractFunction2 implements ValueFunction {


    private org.springframework.security.crypto.password.PasswordEncoder encoder = PasswordEncoderConfig.getEncoder();


    private static final String TYPE_IRI = "http://onto.fel.cvut.cz/ontologies/lib/function/security/matches-password";


    org.springframework.security.crypto.password.PasswordEncoder getEncoder() {
        return encoder;
    }

    void setEncoder(org.springframework.security.crypto.password.PasswordEncoder encoder) {
        this.encoder = encoder;
    }


    @Override
    public String getTypeURI() {
        return TYPE_IRI;
    }

    @Override
    protected NodeValue exec(Node rawPassword, Node encodedPassword, FunctionEnv env) {
        boolean returnVal = encoder.matches(rawPassword.getLiteral().getValue().toString(), encodedPassword.getLiteral().getValue().toString());
        return NodeValue.makeNodeBoolean(returnVal);
    }
}
