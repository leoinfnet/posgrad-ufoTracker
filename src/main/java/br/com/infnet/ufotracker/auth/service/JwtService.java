package br.com.infnet.ufotracker.auth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.cert.X509CertSelector;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class JwtService {
    private static final String JWT_SECRET = "um-segredo-bem-seguro";
    private static final String ISSUER = "UFOTRACKER INC";
    private Algorithm algorithm(){
        return Algorithm.HMAC256(JWT_SECRET);
    }
    public String generateToken(Authentication authenticate) {
        String name = authenticate.getName();
        List<String> roles = authenticate.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .toList();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(1, ChronoUnit.HOURS);
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(name)
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .withClaim("roles", roles)
                .sign(algorithm());
    }
    public String getUsernameFromToken(String token) {
        return decode(token).getSubject();
    }
    public List<String> getRolesFromToken(String token) {
        return decode(token).getClaim("roles").asList(String.class);
    }
    public boolean isValid(String token){
        try {
            decode(token);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    private DecodedJWT decode(String token) {
        return JWT.require(algorithm())
                .withIssuer(ISSUER)
                .build()
                .verify(token);
    }
}
