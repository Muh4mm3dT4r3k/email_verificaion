package com.mohamed.emailVerification.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;
    @Value("${application.security.jwt.expiration}")
    private long expiration;



    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claimResolver(claims, Claims::getSubject);
    }

    public Date extractExpirationDate(String token) {
        Claims claims = extractAllClaims(token);
        return claimResolver(claims, Claims::getExpiration);
    }

    public boolean isExpiredToken(String token) {
        return extractExpirationDate(token).after(new Date(System.currentTimeMillis()));
    }

    public boolean isValidToken(String token, UserDetails userDetails) {
        return extractUsername(token)
                .equals(userDetails.getUsername())
                && !isExpiredToken(token) ;
    }


    private <T> T claimResolver(Claims claims, Function<Claims, T> resolver) {
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }


    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails){
       var authorities = userDetails
               .getAuthorities()
               .stream()
               .map(GrantedAuthority::getAuthority)
               .toList();
       return Jwts
               .builder()
               .setSubject(userDetails.getUsername())
               .claim("authorities", authorities)
               .setIssuedAt(new Date(System.currentTimeMillis()))
               .setExpiration(new Date(System.currentTimeMillis() + expiration))
               .setClaims(extraClaims)
               .signWith(getSignKey())
               .compact();
    }




    private Key getSignKey() {
        byte[] key = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(key);
    }

}
