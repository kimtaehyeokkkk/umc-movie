package com.umc.movie.repository;

import com.umc.movie.entity.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    // 정렬(최신순)과 자를 범위(page, size)는 Pageable로 넘겨받습니다.
    Page<Rating> findByMember_MemberId(Long memberId, Pageable pageable);

    // 언더스코어(_)는 연관 엔티티를 타고 들어가라는 뜻입니다. (rating.member.memberId)
    boolean existsByMember_MemberIdAndMovieId(Long memberId, Long movieId);
}
