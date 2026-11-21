package com.umc.movie.controller;

import com.umc.movie.annotation.LoginMember;
import com.umc.movie.dto.ApiResponse;
import com.umc.movie.dto.CreateFavoriteRequest;
import com.umc.movie.dto.FavoriteResponse;
import com.umc.movie.dto.FavoriteStatusResponse;
import com.umc.movie.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// 우리 응답 DTO 이름(ApiResponse)과 Swagger 애노테이션 이름이 같아서, Swagger 쪽은 전체 경로로 씁니다.
// 인터셉터가 모든 경로를 기본으로 잠그기 때문에 /favorites도 따로 등록하지 않아도 토큰이 필요합니다.
@Tag(name = "Favorite", description = "즐겨찾기 (토큰 필요)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "즐겨찾기 등록", description = "토큰의 회원이 TMDB 영화를 즐겨찾기에 추가합니다. 요청에 memberId는 없습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "movieId 누락 또는 0 이하",
                    content = @Content(examples = @ExampleObject(value = SwaggerExamples.INVALID_MOVIE_ID))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "토큰 없음·위조·만료",
                    content = @Content(examples = @ExampleObject(value = SwaggerExamples.TOKEN_MISSING))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 즐겨찾기한 영화",
                    content = @Content(examples = @ExampleObject(value = SwaggerExamples.FAVORITE_DUPLICATED)))
    })
    @PostMapping
    public ResponseEntity<ApiResponse<FavoriteResponse>> create(
            @LoginMember Long memberId,
            @Valid @RequestBody CreateFavoriteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("즐겨찾기를 등록했습니다.", favoriteService.create(memberId, request)));
    }

    @Operation(summary = "내 즐겨찾기 목록", description = "최근 등록 순으로 응답합니다. 즐겨찾기가 없으면 빈 배열입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공 (빈 배열 포함)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "토큰 없음·위조·만료",
                    content = @Content(examples = @ExampleObject(value = SwaggerExamples.TOKEN_MISSING)))
    })
    @GetMapping
    public ApiResponse<List<FavoriteResponse>> getMyFavorites(@LoginMember Long memberId) {
        return ApiResponse.ok("내 즐겨찾기 목록을 조회했습니다.", favoriteService.getMyFavorites(memberId));
    }

    @Operation(summary = "특정 영화 즐겨찾기 여부", description = "즐겨찾기하지 않았어도 오류가 아니라 200과 isFavorite: false입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "토큰 없음·위조·만료",
                    content = @Content(examples = @ExampleObject(value = SwaggerExamples.TOKEN_MISSING)))
    })
    @GetMapping("/{movieId}")
    public ApiResponse<FavoriteStatusResponse> getStatus(@LoginMember Long memberId, @PathVariable Long movieId) {
        return ApiResponse.ok("즐겨찾기 여부를 조회했습니다.", favoriteService.getStatus(memberId, movieId));
    }

    // 평점 삭제와 같이 공통 응답 형식을 지키려고 204가 아니라 200 + result null로 응답합니다.
    @Operation(summary = "즐겨찾기 해제", description = "성공하면 200과 result: null입니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "해제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "토큰 없음·위조·만료",
                    content = @Content(examples = @ExampleObject(value = SwaggerExamples.TOKEN_MISSING))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "즐겨찾기하지 않은 영화",
                    content = @Content(examples = @ExampleObject(value = SwaggerExamples.FAVORITE_NOT_FOUND)))
    })
    @DeleteMapping("/{movieId}")
    public ApiResponse<Void> delete(@LoginMember Long memberId, @PathVariable Long movieId) {
        favoriteService.delete(memberId, movieId);
        return ApiResponse.ok("즐겨찾기를 해제했습니다.", null);
    }
}
