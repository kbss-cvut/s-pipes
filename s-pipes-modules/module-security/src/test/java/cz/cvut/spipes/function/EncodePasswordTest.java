package cz.cvut.spipes.function;


import org.apache.jena.sparql.expr.NodeValue;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class EncodePasswordTest {

    PasswordEncoder encoder = PasswordEncoderConfig.getEncoder();

    @Test
    public void execReturnsEncodedPassword() {

        EncodePassword EncodePassword = new EncodePassword();
        NodeValue plainPasswordNode = NodeValue.makeNodeString("rawPassword");
        NodeValue encodedPasswordNodeValue = EncodePassword.exec(plainPasswordNode);

        String ahoj = encodedPasswordNodeValue.toString();
        System.out.println(ahoj);
        String encodedP = PasswordEncoderConfig.getEncoder().encode("rawPassword");
        encoder.matches("rawPassword", encodedP);

        assertTrue(encoder.matches("rawPassword", encodedPasswordNodeValue.asString()));
    }

}