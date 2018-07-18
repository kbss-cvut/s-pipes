package cz.cvut.sempipes.function;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class PasswordEncoderConfig {
    static org.springframework.security.crypto.password.PasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder();
    }
}
