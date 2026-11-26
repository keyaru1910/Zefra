package com.paycore.infrastructure.security;

import com.paycore.domain.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Tiện ích tạo và xác thực JWT (HMAC-SHA256) cho PayCore.
 */
@Component
public class JwtTokenProvider {

    private static final String CLAIM_USER_ID = "uid";
    private static final String CLAIM_ROLE = "role";

    private final SecretKey signingKey;
    private final long expirationMs;
    private final String issuer;

    public JwtTokenProvider(
            @Value("${paycore.security.jwt.secret}") String secret,
            @Value("${paycore.security.jwt.expiration-ms:3600000}") long expirationMs,
            @Value("${paycore.security.jwt.issuer:zefra-paycore}") String issuer
    ) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret phải có độ dài tối thiểu 32 bytes (256 bits)");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
        this.issuer = issuer;
    }

    public String generateToken(UUID userId, String username, UserRole role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expiry)
                .claim(CLAIM_USER_ID, userId.toString())
                .claim(CLAIM_ROLE, role.name())
                .signWith(signingKey)
                .compact();
    }

    public Claims parseAndValidateClaims(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(Claims claims) {
        return claims.getSubject();
    }

    public UUID extractUserId(Claims claims) {
        String rawId = claims.get(CLAIM_USER_ID, String.class);
        return rawId != null ? UUID.fromString(rawId) : null;
    }

    public UserRole extractRole(Claims claims) {
        String rawRole = claims.get(CLAIM_ROLE, String.class);
        return rawRole != null ? UserRole.valueOf(rawRole) : UserRole.USER;
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
