package com.umc.movie.controller;

import com.umc.movie.dto.ApiResponse;
import com.umc.movie.dto.CreateRatingRequest;
import com.umc.movie.dto.RatingPageResponse;
import com.umc.movie.dto.RatingResponse;
import com.umc.movie.dto.UpdateRatingRequest;
import com.umc.movie.interceptor.AuthInterceptor;
import com.umc.movie.service.RatingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// 평점 API가 /members 아래와 /ratings로 나뉘어서 클래스 레벨 @RequestMapping을 두지 않습니다.
@RestController
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    // 특정 회원의 평점 목록은 누구나 볼 수 있는 공개 API라 토큰을 받지 않습니다.
    @GetMapping("/members/{memberId}/ratings")
    public ApiResponse<RatingPageResponse> getMemberRatings(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page는 0 이상이어야 합니다.") int page,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "size는 1~50 사이여야 합니다.")
            @Max(value = 50, message = "size는 1~50 사이여야 합니다.") int size) {
        return ApiResponse.ok("평점 목록 조회에 성공했습니다.", ratingService.getRatingsByMember(memberId, page, size));
    }

    // URL에서 /members/{memberId}가 사라졌습니다. 받지 않는 값은 조작당할 수도 없습니다.
    @PostMapping("/ratings")
    public ResponseEntity<ApiResponse<RatingResponse>> create(
            @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @Valid @RequestBody CreateRatingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("평점을 등록했습니다.", ratingService.create(memberId, request)));
    }

    @PatchMapping("/ratings/{ratingId}")
    public ApiResponse<RatingResponse> update(
            @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @PathVariable Long ratingId,
            @Valid @RequestBody UpdateRatingRequest request) {
        return ApiResponse.ok("평점을 수정했습니다.", ratingService.update(memberId, ratingId, request));
    }

    // 204는 본문을 보낼 수 없어 공통 응답 형식을 지킬 수 없으므로, 200 + result null로 응답합니다.
    @DeleteMapping("/ratings/{ratingId}")
    public ApiResponse<Void> delete(
            @RequestAttribute(AuthInterceptor.MEMBER_ID) Long memberId,
            @PathVariable Long ratingId) {
        ratingService.delete(memberId, ratingId);
        return ApiResponse.ok("평점을 삭제했습니다.", null);
    }
}
