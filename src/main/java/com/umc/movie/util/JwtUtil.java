package com.umc.movie.util;

import com.umc.movie.exception.ApiException;
import com.umc.movie.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final Duration accessExpiration;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration}") Long accessExpiration
    ) {
        // 문자열 비밀키를 HMAC-SHA 서명용 키로 바꿉니다. 32바이트보다 짧으면 서버가 뜰 때 예외가 납니다.
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = Duration.ofSeconds(accessExpiration);
    }

    // 로그인에 성공한 회원에게 발급할 Access Token을 만듭니다.
    public String createAccessToken(Long memberId, String email) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(String.valueOf(memberId))  // 토큰의 주인 (memberId)
                .claim("email", email)              // 참고용 부가 정보. payload는 누구나 읽을 수 있으니 비밀번호는 넣지 않습니다
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessExpiration)))
                .signWith(secretKey)                // 내용을 조작하면 이 서명과 맞지 않아 검증에서 걸립니다
                .compact();
    }

    // 토큰을 검증하고 memberId를 꺼냅니다. 값을 돌려줬다면 서명과 만료 검증을 모두 통과한 토큰입니다.
    public Long getMemberId(String token) {
        try {
            return Long.valueOf(getClaims(token).getPayload().getSubject());
        } catch (ExpiredJwtException e) {
            // 만료는 다시 로그인하면 해결되는 상황이라 위조와 구분합니다
            throw new ApiException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            // 서명 불일치, 형식 오류 등 그 외 모든 실패
            throw new ApiException(ErrorCode.TOKEN_INVALID);
        }
    }

    private Jws<Claims> getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)  // 같은 키로 서명을 다시 계산해서 대조합니다
                .clockSkewSeconds(60)   // 서버 간 시계 오차를 60초까지 허용합니다 (만료 판정도 60초 늦어집니다)
                .build()
                .parseSignedClaims(token);
    }
}
