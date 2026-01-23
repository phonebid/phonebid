package com.phonebid.app.common.config;

import org.springframework.context.annotation.Configuration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.phonebid.app.jwt.JwtUtil;
import com.phonebid.app.security.JwtAuthorizationFilter;
import com.phonebid.app.security.UserDetailsServiceImpl;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@Slf4j
public class WebSecurityConfig {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthenticationConfiguration authenticationConfiguration;

    @Value("${cors.allowed-origins}")
    private String allowedOriginsString;

    private List<String> getAllowedOrigins() {
        log.info("CORS allowed origins = {}", allowedOriginsString);
        if (allowedOriginsString == null || allowedOriginsString.isEmpty()) {
            return Arrays.asList("http://localhost:5173", "http://localhost:3000");
        }
        return Arrays.asList(allowedOriginsString.split(","));
    }

    /**
     * Bean으로 직접 수동 등록해서 필터 만들기
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthorizationFilter jwtAuthorizationFilter() {
        return new JwtAuthorizationFilter(jwtUtil, userDetailsService);
    }

    /**
     * WebSocket 엔드포인트를 Spring Security 필터 체인에서 제외
     * 실제 인증은 핸드셰이크 인터셉터에서 처리
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/ws/chat/**");
    }

    /**
     * CORS 설정 - 환경별 origin 분리
     * 보안 강화를 위해 특정 origin만 허용
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 환경별로 설정된 허용 origin 사용
        configuration.setAllowedOrigins(getAllowedOrigins());
        
        // HTTP 메서드 허용
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        
        // 헤더 허용 (더 구체적으로)
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "Origin",
            "X-Requested-With",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // 인증 정보 포함 허용 (쿠키, 인증 헤더)
        configuration.setAllowCredentials(true);
        
        // 노출 헤더 설정
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "Set-Cookie"));
        
        // 캐시 시간 설정 (1시간)
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 등록 후 security Filter에 끼워 넣기
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CSRF 설정
        http.csrf((csrf) -> csrf.disable());

        // CORS 설정
        http.cors((cors) -> cors.configurationSource(corsConfigurationSource()));

        http.sessionManagement((sessionManagement) ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)  
        );

        http.authorizeHttpRequests((authorizeHttpRequests) ->
                authorizeHttpRequests
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll() // resources 접근 허용 설정
                        .requestMatchers("/").permitAll() // 메인 페이지 요청 허가
                        .requestMatchers("/api/v1/users/signup").permitAll() // 회원가입 엔드포인트 접근 허가
                        .requestMatchers("/api/v1/users/login").permitAll() // 로그인 엔드포인트 접근 허가
                        .requestMatchers("/api/v1/sellers/register").permitAll() // 판매자 회원가입 엔드포인트 접근 허가
                        .requestMatchers("/api/v1/sellers/documents/temp").permitAll() // 임시 파일 업로드 엔드포인트 접근 허가 (회원가입 단계용)
                        .requestMatchers("/api/v1/auth/kakao/**").permitAll() // 카카오 OAuth 엔드포인트 접근 허가
                        .requestMatchers("/api/v1/auth/naver/**").permitAll() // 네이버 OAuth 엔드포인트 접근 허가
                        .requestMatchers("/api/v1/payments/portone/**").permitAll() // PortOne 결제 엔드포인트 접근 허가
                        .anyRequest().authenticated() // 그 외 모든 요청 인증처리
        );

        // http.formLogin((formLogin) ->
        //         formLogin
        //                 .loginPage("/api/user/login-page").permitAll()
        // );

        // 필터 관리 (인가 필터만 처리)
        http.addFilterBefore(jwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);

        // 관리자 페이지 접근 불가 설정
        // http.exceptionHandling((exceptionHandling) ->
        //         exceptionHandling
        //                 .accessDeniedPage("/forbidden.html")
        // );

        return http.build();
    }

}
