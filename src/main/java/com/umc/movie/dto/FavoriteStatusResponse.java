package com.umc.movie.dto;

// 즐겨찾기하지 않은 것도 오류가 아니라 정상 상태라서 false로 응답합니다.
public record FavoriteStatusResponse(boolean isFavorite) {}
