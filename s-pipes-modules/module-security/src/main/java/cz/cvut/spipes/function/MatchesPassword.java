package cz.cvut.spipes.function;

import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;

public class MatchesPassword extends FunctionBase2 implements ValueFunction {


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
    public NodeValue exec(NodeValue rawPassword, NodeValue encodedPassword) {
        boolean returnVal = encoder.matches(
                rawPassword.asNode().getLiteral().getValue().toString(),
                encodedPassword.asNode().getLiteral().getValue().toString()
        );
        return NodeValue.makeNodeBoolean(returnVal);
    }
}
