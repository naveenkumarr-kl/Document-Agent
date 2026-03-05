package com.example.capstone_project.util;

import com.example.capstone_project.entity.User;
import com.example.capstone_project.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    @Autowired
    UserRepository userRepository;

    private final Key SECRET_KEY;
    private final long TOKEN_EXPIRY;

    public JwtUtil(@Value("${jwt.secret}") String SECRET,
                   @Value("${jwt.expirationMs}") long TOKEN_EXPIRY)
    {
        this.SECRET_KEY= Keys.hmacShaKeyFor(SECRET.getBytes());
        this.TOKEN_EXPIRY=TOKEN_EXPIRY;
    }

    public String generateToken(User user)
    {
        String jwtToken = Jwts.builder()
                .subject(user.getEmail())
                .claim("id", user.getUid())
                .claim("role",user.getRole().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRY))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();

        return jwtToken;
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public Date extractExpiration(String token) {
        return extractClaims(token).getExpiration();
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token, String username) {

        return username.equals(extractUsername(token))
                && !isTokenExpired(token);
    }
}
