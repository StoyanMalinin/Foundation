package foundation.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class TokenManager {
    private final String secrtet;
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public TokenManager(String secret) {
        this.secrtet = secret;

        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(algorithm).build();
    }

    public String generateToken(String username) {
        Instant now = Instant.now();
        return JWT.create()
                .withSubject(username)
                .withExpiresAt(Date.from(now.plus(30, ChronoUnit.MINUTES)))
                .sign(this.algorithm);
    }

    public String getUsernameFromToken(String token) throws JWTVerificationException {
        DecodedJWT jwt = verifier.verify(token);
        return jwt.getSubject();
    }
}
