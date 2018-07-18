package cz.cvut.spipes.function;

import cz.cvut.spipes.function.ARQFunction;
import cz.cvut.spipes.function.ValueFunction;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionEnv;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.topbraid.spin.arq.AbstractFunction1;

public class EncodePassword extends AbstractFunction1 implements ValueFunction {


    private org.springframework.security.crypto.password.PasswordEncoder encoder = PasswordEncoderConfig.getEncoder();


    private static final String TYPE_IRI = "http://onto.fel.cvut.cz/ontologies/lib/function/security/encode-password";


    org.springframework.security.crypto.password.PasswordEncoder getEncoder() {
        return encoder;
    }

    void setEncoder(org.springframework.security.crypto.password.PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    protected NodeValue exec(Node rawPassword, FunctionEnv env) {
        String encodedPassword = encoder.encode(rawPassword.getLiteral().getValue().toString());
        return NodeValue.makeNodeString(encodedPassword);
    }

    @Override
    public String getTypeURI() {
        return TYPE_IRI;
    }
}
