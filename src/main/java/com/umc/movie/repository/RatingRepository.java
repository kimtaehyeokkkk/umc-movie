package com.umc.movie.repository;

import com.umc.movie.entity.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    // 정렬(최신순)과 자를 범위(page, size)는 Pageable로 넘겨받습니다.
    Page<Rating> findByMember_MemberId(Long memberId, Pageable pageable);
}
