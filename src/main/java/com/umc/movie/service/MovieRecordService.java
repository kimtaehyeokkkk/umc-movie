package com.umc.movie.service;

import com.umc.movie.dto.CreateMovieRecordRequest;
import com.umc.movie.dto.FavoriteResponse;
import com.umc.movie.dto.MovieRecordResponse;
import com.umc.movie.dto.RatingResponse;
import com.umc.movie.entity.Favorite;
import com.umc.movie.entity.Member;
import com.umc.movie.entity.Rating;
import com.umc.movie.exception.ApiException;
import com.umc.movie.exception.ErrorCode;
import com.umc.movie.repository.FavoriteRepository;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// "평점을 남기고 즐겨찾기에도 넣기"라는 한 가지 사용자 행동을 처리합니다.
// RatingService.create()와 FavoriteService.create()를 따로 부르지 않고, 두 저장을 이 메서드 하나의 트랜잭션으로 묶습니다.
@Service
@RequiredArgsConstructor
public class MovieRecordService {

    private final MemberRepository memberRepository;
    private final RatingRepository ratingRepository;
    private final FavoriteRepository favoriteRepository;

    // 메서드가 정상적으로 끝나면 commit, 런타임 예외가 밖으로 나가면 평점 저장까지 모두 rollback됩니다.
    @Transactional
    public MovieRecordResponse create(Long memberId, CreateMovieRecordRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        // 저장을 시작하기 전에 두 조건을 모두 확인합니다.
        if (ratingRepository.existsByMember_MemberIdAndMovieId(memberId, request.movieId())) {
            throw new ApiException(ErrorCode.RATING_DUPLICATED);
        }
        if (request.addToFavorites()
                && favoriteRepository.existsByMember_MemberIdAndMovieId(memberId, request.movieId())) {
            throw new ApiException(ErrorCode.FAVORITE_DUPLICATED);
        }

        Rating rating = ratingRepository.save(Rating.builder()
                .member(member)
                .movieId(request.movieId())
                .score(request.score())
                .comment(request.comment())
                .build());

        FavoriteResponse favoriteResponse = null;
        if (request.addToFavorites()) {
            // 사전 검사 뒤에 다른 요청이 같은 즐겨찾기를 먼저 저장했다면 여기서 UNIQUE 제약에 걸리고,
            // 위에서 저장한 평점도 함께 rollback되어 "평점만 저장된" 상태가 남지 않습니다.
            Favorite favorite = favoriteRepository.save(Favorite.builder()
                    .member(member)
                    .movieId(request.movieId())
                    .build());
            favoriteResponse = FavoriteResponse.from(favorite);
        }

        return new MovieRecordResponse(RatingResponse.from(rating), favoriteResponse);
    }
}
