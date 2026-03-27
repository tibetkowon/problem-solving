package com.problemsolving.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("단원별 문제 풀이 API")
                        .description("""
                                단원별 CS 문제 풀이 및 풀이 이력 조회 API입니다.

                                ## 주요 기능
                                - **단원 조회**: 전체 단원 목록 조회
                                - **문제 조회**: 단원별 랜덤 문제 조회 (미풀이, 직전 건너뜀 제외)
                                - **문제 제출**: 객관식(단일/복수)/주관식 답안 제출 및 즉시 채점
                                - **풀이 이력**: 사용자별 풀이 목록 및 상세 조회

                                ## 정답 판정 기준
                                - `CORRECT` : 정답 완전 일치
                                - `PARTIAL` : 객관식 복수 정답 일부 일치
                                - `WRONG`   : 오답
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Problem Solving API")
                        )
                )
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("로컬 개발 서버")
                ));
    }
}
