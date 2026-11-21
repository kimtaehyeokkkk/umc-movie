package com.umc.movie.config;

import com.umc.movie.annotation.LoginMember;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    static {
        // @LoginMember 파라미터는 요청 값이 아니라 토큰에서 오므로 문서의 입력 칸에서 숨깁니다.
        SpringDocUtils.getConfig().addAnnotationsToIgnore(LoginMember.class);
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("UMC 영화 평점 서비스 API")
                        .version("v1")
                        .description("자물쇠가 있는 API는 Authorization: Bearer {accessToken} 헤더가 필요합니다. "
                                + "오른쪽 위 Authorize에 POST /auth/login 응답의 accessToken을 넣으면 됩니다."))
                // 서버도 "전부 잠그고 공개 API만 연다" 방식이라, 문서도 기본으로 토큰을 요구하고 공개 API에서만 해제합니다.
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components().addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
