package com.umc.movie.resolver;

import com.umc.movie.annotation.LoginMember;
import com.umc.movie.exception.ApiException;
import com.umc.movie.exception.ErrorCode;
import com.umc.movie.interceptor.AuthInterceptor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

// @LoginMember Long memberId 파라미터에 값을 채워 주는 역할입니다.
// "memberId"라는 속성 이름은 여기와 인터셉터만 알고, 컨트롤러는 몰라도 됩니다.
@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMember.class)
                && Long.class.equals(parameter.getParameterType());
    }

    @Override
    public Long resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Object memberId = webRequest.getAttribute(AuthInterceptor.MEMBER_ID, RequestAttributes.SCOPE_REQUEST);

        // 인터셉터가 열어 둔(토큰 검사를 건너뛴) 경로에서 @LoginMember를 쓰면 값이 없습니다.
        if (memberId == null) {
            throw new ApiException(ErrorCode.TOKEN_MISSING);
        }
        return (Long) memberId;
    }
}
