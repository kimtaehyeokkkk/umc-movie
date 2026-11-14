package com.umc.movie.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 컨트롤러 파라미터에 붙이면, 인터셉터가 토큰에서 꺼낸 로그인 회원의 memberId가 들어옵니다.
// 이름만 보고도 "이 값은 요청이 아니라 로그인한 사용자에게서 온다"는 게 드러나도록 만들었습니다.
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginMember {
}
