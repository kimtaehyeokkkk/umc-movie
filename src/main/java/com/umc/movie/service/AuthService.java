package com.umc.movie.service;

import com.umc.movie.dto.LoginRequest;
import com.umc.movie.dto.LoginResponse;
import com.umc.movie.entity.Member;
import com.umc.movie.exception.ApiException;
import com.umc.movie.exception.ErrorCode;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // 이메일이 없어도 "가입되지 않은 이메일"이라고 알려주지 않습니다.
        // 비밀번호가 틀린 경우와 같은 응답을 줘야 가입 여부를 알아낼 수 없습니다.
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(ErrorCode.LOGIN_FAILED));

        // 저장된 해시에서 salt를 꺼내 입력값을 같은 방식으로 해시한 뒤 비교합니다.
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new ApiException(ErrorCode.LOGIN_FAILED);
        }

        String accessToken = jwtUtil.createAccessToken(member.getMemberId(), member.getEmail());
        return new LoginResponse(member.getNickname(), accessToken);
    }
}
