package com.ideahub.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long tokenExpirationMs;

    public JwtTokenProvider(@Value("${app.jwt.secret}") String secret,
                            @Value("${app.jwt.access-token-expiration-ms}") long tokenExpirationMs) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "Missing JWT configuration: set JWT_SECRET to a base64-encoded secret before starting the application."
            );
        }

        try {
            this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret.trim()));
        } catch (RuntimeException ex) {
            throw new IllegalStateException(
                    "Invalid JWT configuration: JWT_SECRET must be a base64-encoded key with at least 32 bytes once decoded.",
                    ex
            );
        }
        this.tokenExpirationMs = tokenExpirationMs;
    }

    public String generateToken(UserPrincipal principal) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + tokenExpirationMs);

        return Jwts.builder()
                .subject(principal.getUsername())
                .issuedAt(now)
                .expiration(expiry)
                .claim("uid", principal.getId())
                .signWith(secretKey)
                .compact();
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        Date expiration = getClaims(token).getExpiration();
        return username.equals(userDetails.getUsername()) && expiration.after(new Date());
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
