package topg.Event_Platform.config;


import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import topg.Event_Platform.exceptions.InvalidJwtToken;

import java.security.Key;
import java.util.Date;
import java.util.List;


@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private String jwtExpiration;
    public String getJwtFromHeader(HttpServletRequest httpServletRequest){
        String bearerToken = httpServletRequest.getHeader("Authorization");
        if(bearerToken != null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }

    public  String generateToken(UserDetailsServiceImpl userDetailsService){
        String name = userDetailsService.getUsername();
        List<String> roles = userDetailsService.getAuthorities().stream()
                .map(authority -> "ROLE_" + authority.getAuthority())
                .toList();
        long expirationMilliseconds = Long.parseLong(jwtExpiration);
        return Jwts.builder()
                .subject(name)
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMilliseconds))
                .signWith(key())
                .compact();
    }

    public List<String> getRolesFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("roles", List.class);
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(key())
                .build()
                .parseSignedClaims(token)
                .getPayload().getSubject();}

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(key())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidJwtToken("Invalid JWT token");
        }
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }



}
