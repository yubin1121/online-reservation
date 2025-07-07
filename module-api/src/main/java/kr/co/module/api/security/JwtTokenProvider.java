package kr.co.module.api.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import kr.co.module.core.domain.JwtTokenResponse;
import kr.co.module.core.domain.User;
import kr.co.module.mapper.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class JwtTokenProvider {
    private static final long ACCESS_TOKEN_VALID_PERIOD = 1000L * 60 * 60 * 24 * 8; //8일
    private final Key jwtSecretKey;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;

    public JwtTokenProvider(@Value("${jwt.secret-key}") String secretKey, UserDetailsService userDetailsService, UserRepository userRepository) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        jwtSecretKey = Keys.hmacShaKeyFor(keyBytes);
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    public JwtTokenResponse generateJWT(final User userInfo) {
        final Date now = new Date();
        final Date accessTokenExpireIn = new Date(now.getTime() + ACCESS_TOKEN_VALID_PERIOD);

        final String accessToken = Jwts.builder()
                .setSubject("authorization") // 토큰 용도
                .claim("userUuid", userInfo.getEmail()) // Claims 설정
                .claim("role", userInfo.getRole())
                .setExpiration(accessTokenExpireIn) // 토큰 만료 시간 설정
                .signWith(jwtSecretKey, SignatureAlgorithm.HS256) // HS256과 Key로 Sign
                .compact(); // 토큰 생성


        return new JwtTokenResponse(accessToken, accessTokenExpireIn.getTime());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(jwtSecretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("유효하지 않은 토큰: {}", e.getMessage());
            return false;
        }
    }
}
