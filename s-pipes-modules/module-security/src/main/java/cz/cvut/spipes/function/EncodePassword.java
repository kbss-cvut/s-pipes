package cz.cvut.spipes.function;

import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

public class EncodePassword extends FunctionBase1 implements ValueFunction {


    private org.springframework.security.crypto.password.PasswordEncoder encoder = PasswordEncoderConfig.getEncoder();


    private static final String TYPE_IRI = "http://onto.fel.cvut.cz/ontologies/lib/function/security/encode-password";


    org.springframework.security.crypto.password.PasswordEncoder getEncoder() {
        return encoder;
    }

    void setEncoder(org.springframework.security.crypto.password.PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public NodeValue exec(NodeValue rawPassword) {
        String encodedPassword = encoder.encode(rawPassword.asNode().getLiteral().getValue().toString());
        return NodeValue.makeNodeString(encodedPassword);
    }

    @Override
    public String getTypeURI() {
        return TYPE_IRI;
    }
}
