package dev.matheuslf.desafio.inscritos.service.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import dev.matheuslf.desafio.inscritos.service.token.exception.InvalidTokenException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@Slf4j
public class TokenService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.issuer}")
    private String issuer;

    @Value("${app.jwt.expiration.hours}")
    private long expirationHours;

    private Algorithm algorithm;
    private JWTVerifier verifier;

    @PostConstruct
    public void validateProperties() {
        if (secret == null || secret.isBlank() || secret.startsWith("${")) {
            log.error("JWT secret is not configured properly. Please set 'app.jwt.secret' (e.g. via environment variable JWT_SECRET_KEY).");
            throw new IllegalStateException("JWT secret is not configured. Please set 'app.jwt.secret'.");
        }
        if (issuer == null || issuer.isBlank()) {
            log.error("JWT issuer is not configured. Please set 'app.jwt.issuer'.");
            throw new IllegalStateException("JWT issuer is not configured. Please set 'app.jwt.issuer'.");
        }
        if (expirationHours <= 0) {
            log.error("JWT expiration.hours must be greater than 0. Current value: {}", expirationHours);
            throw new IllegalStateException("JWT expiration.hours must be > 0.");
        }
        algorithm = Algorithm.HMAC256(secret);
        verifier = JWT.require(algorithm).withIssuer(issuer).build();
        log.info("JWT properties validated successfully. issuer={}, expirationHours={}", issuer, expirationHours);
    }

    // now include role claim
    public String generateToken(String username, String role) {
        Instant now = Instant.now();
        Instant expiration = now.plus(expirationHours, ChronoUnit.HOURS);

        return JWT.create()
                .withIssuer(issuer)
                .withSubject(username)
                .withClaim("role", role)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(expiration))
                .sign(algorithm);
    }

    public String validateToken(String token) {
        try {
            DecodedJWT decodedJWT = verifier.verify(token);
            String subject = decodedJWT.getSubject();
            log.debug("Token validated for subject={}", subject);
            return subject;
        } catch (JWTVerificationException exception){
            log.warn("Token validation failed: {}", exception.getMessage());
            throw new InvalidTokenException("Token inválido, expirado ou malformado", exception);
        }
    }

    public String getRoleFromToken(String token) {
        try {
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT.getClaim("role").asString();
        } catch (JWTVerificationException exception) {
            log.warn("Failed to extract role from token: {}", exception.getMessage());
            return null;
        }
    }
}
