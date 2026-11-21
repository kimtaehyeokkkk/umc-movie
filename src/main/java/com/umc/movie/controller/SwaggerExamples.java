package com.umc.movie.controller;

// Swagger 문서에 보여줄 실패 응답 예시입니다. 애노테이션 값은 상수여야 해서 한 곳에 모았습니다.
final class SwaggerExamples {

    private SwaggerExamples() {
    }

    static final String INVALID_MOVIE_ID = """
            {"isSuccess": false, "code": "COMMON400_1", "message": "영화 ID는 양수여야 합니다.", "result": null}""";

    static final String TOKEN_MISSING = """
            {"isSuccess": false, "code": "AUTH401_2", "message": "로그인이 필요한 요청입니다.", "result": null}""";

    static final String FAVORITE_DUPLICATED = """
            {"isSuccess": false, "code": "FAVORITE409_1", "message": "이미 이 영화를 즐겨찾기하셨습니다.", "result": null}""";

    static final String FAVORITE_NOT_FOUND = """
            {"isSuccess": false, "code": "FAVORITE404_1", "message": "즐겨찾기한 영화를 찾을 수 없습니다.", "result": null}""";

    static final String RATING_DUPLICATED = """
            {"isSuccess": false, "code": "RATING409_1", "message": "이미 이 영화에 평점을 남기셨습니다.", "result": null}""";
}
