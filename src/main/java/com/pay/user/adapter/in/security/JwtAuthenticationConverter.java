package com.pay.user.adapter.in.security;

import com.pay.user.domain.User;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationConverter {

    public Authentication convert(Claims claims) {
        String email = claims.getSubject();
        Long userId = claims.get("uid", Long.class);
        User stub = User.restoreForAuth(userId, email);
        CustomUserDetails principal = new CustomUserDetails(stub);

        return new UsernamePasswordAuthenticationToken(principal, "", principal.getAuthorities());
    }
}
