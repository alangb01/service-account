package pe.nom.charlygastelo.app.accountservice.security;
// ... existing code ...

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.reactivex.rxjava3.core.Single;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class JwtTokenValidator {

    @Value("${jwt.secret}")
    private String secret;

    public Single<UserDetails> validate(String token) {
        return Single.fromCallable(() -> {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String username = claims.getSubject();
            List<String> roles = claims.get("roles", List.class);
            return User
                    .withUsername(username)
                    .password("")
                    .roles(roles.toArray(new String[0]))
                    .build();
        });
    }
}