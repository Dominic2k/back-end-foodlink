package org.datpham.foodlink.security;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class JwtTokenProvider {

    // TODO: Implement JWT creation and validation when you decide the library.
    public String generateToken(String subject) {
        return "DUMMY-" + subject + "-" + UUID.randomUUID();
    }

    public boolean validateToken(String token) {
        return token != null && token.startsWith("DUMMY-");
    }

    public String getSubject(String token) {
        if (token == null) {
            return null;
        }
        String[] parts = token.split("-", 3);
        return parts.length >= 2 ? parts[1] : null;
    }
}
