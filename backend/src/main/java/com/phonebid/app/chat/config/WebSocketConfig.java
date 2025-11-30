package com.phonebid.app.chat.config;

import com.phonebid.app.jwt.JwtUtil;
import com.phonebid.app.security.UserDetailsImpl;
import com.phonebid.app.security.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new JwtHandshakeInterceptor(jwtUtil, userDetailsService))
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
        registry.enableSimpleBroker("/topic", "/queue");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null) {
                    return message;
                }

                // STOMP CONNECT 프레임에서 토큰 추출 및 인증 처리
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    try {
                        // CONNECT 프레임의 헤더에서 Authorization 추출
                        String authHeader = accessor.getFirstNativeHeader("Authorization");
                        String token = null;
                        
                        if (authHeader != null && StringUtils.hasText(authHeader) && authHeader.startsWith(JwtUtil.BEARER_PREFIX)) {
                            token = authHeader.substring(JwtUtil.BEARER_PREFIX.length());
                        }

                        if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                            Claims claims = jwtUtil.getUserInfoFromToken(token);
                            String username = claims.getSubject();

                            UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);
                            Authentication authentication = new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                            SecurityContextHolder.getContext().setAuthentication(authentication);

                            // 세션 속성에 사용자 정보 저장
                            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
                            if (sessionAttributes != null) {
                                sessionAttributes.put("username", username);
                                sessionAttributes.put("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
                                sessionAttributes.put(Principal.class.getName(), userDetails);
                            }

                            StompHeaderAccessor mutableAccessor = StompHeaderAccessor.wrap(message);
                            mutableAccessor.setUser(authentication);
                            
                            log.info("STOMP CONNECT 인증 성공: username={}", username);
                            return MessageBuilder.createMessage(message.getPayload(), mutableAccessor.getMessageHeaders());
                        } else {
                            log.warn("STOMP CONNECT 인증 실패: 토큰이 없거나 유효하지 않습니다.");
                            // 인증 실패 시 연결 거부 (null 반환 시 메시지가 차단됨)
                            return null;
                        }
                    } catch (Exception e) {
                        log.error("STOMP CONNECT 인증 처리 중 오류 발생", e);
                        return null;
                    }
                }

                // MESSAGE 프레임 처리 (기존 로직 유지)
                if (StompCommand.MESSAGE.equals(accessor.getCommand())) {
                    Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
                    if (sessionAttributes != null) {
                        String username = (String) sessionAttributes.get("username");
                        if (username != null) {
                            try {
                                UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);
                                Authentication authentication = new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                
                                StompHeaderAccessor mutableAccessor = StompHeaderAccessor.wrap(message);
                                mutableAccessor.setUser(authentication);
                                return MessageBuilder.createMessage(message.getPayload(), mutableAccessor.getMessageHeaders());
                            } catch (Exception e) {
                                log.error("메시지 전송 전 인증 정보 설정 실패: username={}", username, e);
                            }
                        }
                    }
                }
                return message;
            }
        });
    }

    /**
     * WebSocket 핸드셰이크 시 JWT 토큰을 검증하고 인증 정보를 설정하는 인터셉터
     */
    @RequiredArgsConstructor
    private static class JwtHandshakeInterceptor implements HandshakeInterceptor {

        private final JwtUtil jwtUtil;
        private final UserDetailsServiceImpl userDetailsService;

        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                      WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
            // 핸드셰이크는 기본적으로 허용
            // 실제 인증은 STOMP CONNECT 프레임에서 처리하므로 여기서는 기본 검증만 수행
            // 하위 호환성을 위해 쿼리 파라미터 토큰이 있으면 검증 (fallback)
            try {
                if (request instanceof ServletServerHttpRequest) {
                    ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
                    HttpServletRequest httpRequest = servletRequest.getServletRequest();

                    // 하위 호환성: 쿼리 파라미터에 토큰이 있으면 검증 (fallback)
                    String token = httpRequest.getParameter("token");
                    if (StringUtils.hasText(token)) {
                        // Bearer 접두사 제거
                        if (token.startsWith(JwtUtil.BEARER_PREFIX)) {
                            token = token.substring(JwtUtil.BEARER_PREFIX.length());
                        }

                        if (jwtUtil.validateToken(token)) {
                            Claims claims = jwtUtil.getUserInfoFromToken(token);
                            String username = claims.getSubject();

                            UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);
                            Authentication authentication = new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());

                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            
                            // WebSocket 메시지 핸들러에서 Principal로 접근할 수 있도록 설정
                            attributes.put("username", username);
                            attributes.put("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
                            attributes.put(Principal.class.getName(), userDetails);

                            log.info("WebSocket 핸드셰이크 성공 (쿼리 파라미터): username={}", username);
                        } else {
                            log.warn("WebSocket 핸드셰이크: 쿼리 파라미터 토큰이 유효하지 않습니다. STOMP CONNECT에서 재시도합니다.");
                        }
                    } else {
                        // 쿼리 파라미터에 토큰이 없으면 STOMP CONNECT 프레임에서 처리하도록 허용
                        log.debug("WebSocket 핸드셰이크: 쿼리 파라미터에 토큰이 없습니다. STOMP CONNECT에서 처리합니다.");
                    }
                    
                    // 기본적으로 핸드셰이크 허용 (실제 인증은 CONNECT 프레임에서 처리)
                    return true;
                }
            } catch (Exception e) {
                log.error("WebSocket 핸드셰이크 중 오류 발생", e);
                // 오류 발생 시에도 핸드셰이크 허용 (CONNECT 프레임에서 인증 실패 처리)
                return true;
            }
            return true;
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                  WebSocketHandler wsHandler, Exception exception) {
            // 핸드셰이크 완료 후 처리할 로직이 있다면 여기에 작성
        }
    }
}


