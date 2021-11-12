package io.vdev.util;

import io.smallrye.jwt.build.Jwt;
import org.eclipse.microprofile.jwt.Claims;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;


@ApplicationScoped
public class JWTTokenUtil {
    public String generateToken(String username, String email, String role) throws IOException, GeneralSecurityException {
        return Jwt.issuer("http://startup-community")
                .upn(email)
                .groups(Collections.unmodifiableSet(Collections.singleton(role)))
                .claim(Claims.preferred_username, username)
                .sign();
    }
}
