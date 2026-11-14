package com.umc.movie.interceptor;

import com.umc.movie.exception.ApiException;
import com.umc.movie.exception.ErrorCode;
import com.umc.movie.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    public static final String MEMBER_ID = "memberId"; // 컨트롤러에서 꺼낼 때 쓰는 이름
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    // 컨트롤러가 실행되기 전에 호출됩니다. true면 컨트롤러로 이어지고, 예외를 던지면 여기서 끝납니다.
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 브라우저의 CORS 사전 요청(preflight)에는 토큰이 붙지 않으므로 그대로 통과시킵니다
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        // 인증 방식 이름은 대소문자를 구분하지 않으므로(RFC 7235) "bearer "도 받습니다. 토큰 부분은 그대로 씁니다.
        if (header == null || !header.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            throw new ApiException(ErrorCode.TOKEN_MISSING);
        }

        // 검증에 실패하면 JwtUtil이 예외를 던지므로, 여기까지 왔다면 유효한 토큰입니다
        Long memberId = jwtUtil.getMemberId(header.substring(BEARER_PREFIX.length()));

        // 뒤이어 실행될 컨트롤러가 꺼내 쓸 수 있도록 요청에 담아둡니다
        request.setAttribute(MEMBER_ID, memberId);
        return true;
    }
}
