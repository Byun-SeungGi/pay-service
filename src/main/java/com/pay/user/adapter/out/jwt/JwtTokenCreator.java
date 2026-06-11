package com.pay.user.adapter.out.jwt;

import com.pay.user.application.port.out.TokenProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenCreator implements TokenProvider {

    private final Key key;
    private final long accessExpiration;

    public JwtTokenCreator(@Value("${jwt.secret}") String secretKey,
                            @Value("${jwt.access-expiration}") long accessExpiration) {

        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);

        this.accessExpiration = accessExpiration * 1000;
    }

    public String createToken(String email, Long userId) {
        long now = (new Date()).getTime();
        Date validty = new Date(now + this.accessExpiration);

        return Jwts.builder()
                .setSubject(email)
                .claim("uid", userId)
                .setIssuedAt(new Date())
                .setExpiration(validty)
                .signWith(this.key, SignatureAlgorithm.HS256)
                .compact();
    }
}