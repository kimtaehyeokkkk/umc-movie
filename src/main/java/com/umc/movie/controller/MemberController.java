package com.umc.movie.controller;

import com.umc.movie.annotation.LoginMember;
import com.umc.movie.dto.ApiResponse;
import com.umc.movie.dto.EmailCheckResponse;
import com.umc.movie.dto.MemberInfoResponse;
import com.umc.movie.dto.MemberResponse;
import com.umc.movie.dto.MemberUpdateRequest;
import com.umc.movie.dto.NicknameCheckResponse;
import com.umc.movie.dto.SignUpRequest;
import com.umc.movie.service.MemberService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member", description = "회원가입·중복 확인(공개), 내 정보(토큰 필요)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    // 형식이 틀리면 Service(DB 조회)까지 가지 않고 바로 400으로 응답합니다.
    @SecurityRequirements
    @GetMapping("/nickname/{nickname}")
    public ApiResponse<NicknameCheckResponse> checkNickname(
            @PathVariable @Size(min = 2, max = 12, message = "닉네임은 2~12자여야 합니다.") String nickname) {
        return ApiResponse.ok("닉네임 중복 확인에 성공했습니다.", memberService.checkNickname(nickname));
    }

    @SecurityRequirements
    @GetMapping("/email/{email}")
    public ApiResponse<EmailCheckResponse> checkEmail(
            @PathVariable @Email(message = "이메일 형식이 올바르지 않습니다.") String email) {
        return ApiResponse.ok("이메일 중복 확인에 성공했습니다.", memberService.checkEmail(email));
    }

    @SecurityRequirements
    @PostMapping
    public ResponseEntity<ApiResponse<MemberResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
        MemberResponse response = memberService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("회원가입에 성공했습니다.", response));
    }

    // URL 어디에도 누구의 정보인지 적혀 있지 않습니다. 인터셉터가 토큰에서 꺼내 담아둔 memberId를 씁니다.
    @GetMapping("/me")
    public ApiResponse<MemberInfoResponse> getMyInfo(@LoginMember Long memberId) {
        return ApiResponse.ok("회원 정보를 조회했습니다.", memberService.getMyInfo(memberId));
    }

    @PatchMapping("/me")
    public ApiResponse<MemberInfoResponse> updateMyInfo(
            @LoginMember Long memberId,
            @Valid @RequestBody MemberUpdateRequest request) {
        return ApiResponse.ok("회원 정보를 수정했습니다.", memberService.updateMyInfo(memberId, request));
    }
}
