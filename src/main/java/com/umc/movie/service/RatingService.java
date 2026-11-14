package com.umc.movie.service;

import com.umc.movie.dto.CreateRatingRequest;
import com.umc.movie.dto.RatingPageResponse;
import com.umc.movie.dto.RatingResponse;
import com.umc.movie.dto.UpdateRatingRequest;
import com.umc.movie.entity.Member;
import com.umc.movie.entity.Rating;
import com.umc.movie.exception.ApiException;
import com.umc.movie.exception.ErrorCode;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 7주차부터 memberId는 클라이언트가 아니라 토큰에서 옵니다.
// "누가"가 모든 동작의 주어라서 인자 순서를 (memberId, ratingId)로 통일했습니다.
@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public RatingPageResponse getRatingsByMember(Long memberId, int page, int size) {
        if (!memberRepository.existsById(memberId)) {
            throw new ApiException(ErrorCode.MEMBER_NOT_FOUND);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ratingId"));
        return RatingPageResponse.from(ratingRepository.findByMember_MemberId(memberId, pageable));
    }

    @Transactional
    public RatingResponse create(Long memberId, CreateRatingRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));

        // rating 테이블에는 (member_id, movie_id) 복합 UNIQUE 제약이 있습니다.
        // DB도 막아주지만, 우리가 정한 code와 message로 응답하려고 먼저 확인합니다.
        if (ratingRepository.existsByMember_MemberIdAndMovieId(memberId, request.movieId())) {
            throw new ApiException(ErrorCode.RATING_DUPLICATED);
        }

        Rating rating = Rating.builder()
                .member(member)
                .movieId(request.movieId())
                .score(request.score())
                .comment(request.comment())
                .build();

        return RatingResponse.from(ratingRepository.save(rating));
    }

    @Transactional
    public RatingResponse update(Long memberId, Long ratingId, UpdateRatingRequest request) {
        Rating rating = findOwnedRating(memberId, ratingId);
        rating.update(request.score(), request.comment());

        // @UpdateTimestamp는 flush 시점에 채워집니다. 응답에 갱신된 updatedAt을 담으려고 먼저 flush합니다.
        ratingRepository.flush();
        return RatingResponse.from(rating);
    }

    @Transactional
    public void delete(Long memberId, Long ratingId) {
        ratingRepository.delete(findOwnedRating(memberId, ratingId));
    }

    // 인증(누구인지)은 인터셉터가, 인가(이 평점이 네 것인지)는 여기서 확인합니다.
    // 먼저 평점이 있는지 보고(없으면 404), 있으면 주인이 맞는지 봅니다(아니면 403).
    private Rating findOwnedRating(Long memberId, Long ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ApiException(ErrorCode.RATING_NOT_FOUND));

        if (!rating.getMember().getMemberId().equals(memberId)) {
            throw new ApiException(ErrorCode.RATING_FORBIDDEN);
        }

        return rating;
    }
}
