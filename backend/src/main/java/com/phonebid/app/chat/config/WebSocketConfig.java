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
                if (accessor != null && StompCommand.MESSAGE.equals(accessor.getCommand())) {
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
            try {
                if (request instanceof ServletServerHttpRequest) {
                    ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
                    HttpServletRequest httpRequest = servletRequest.getServletRequest();

                    // 헤더에서 토큰 추출 시도
                    String token = jwtUtil.getJwtFromHeader(httpRequest);
                    
                    // 헤더에 토큰이 없으면 쿼리 파라미터에서 추출 (SockJS 호환성)
                    if (!StringUtils.hasText(token)) {
                        token = httpRequest.getParameter("token");
                        // 쿼리 파라미터의 토큰이 Bearer 접두사를 포함하지 않으면 제거
                        if (StringUtils.hasText(token) && token.startsWith(JwtUtil.BEARER_PREFIX)) {
                            token = token.substring(JwtUtil.BEARER_PREFIX.length());
                        }
                    }

                    if (!StringUtils.hasText(token)) {
                        log.warn("WebSocket 핸드셰이크 실패: JWT 토큰이 없습니다.");
                        return false;
                    }

                    if (!jwtUtil.validateToken(token)) {
                        log.warn("WebSocket 핸드셰이크 실패: 유효하지 않은 JWT 토큰입니다.");
                        return false;
                    }

                    Claims claims = jwtUtil.getUserInfoFromToken(token);
                    String username = claims.getSubject();

                    UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    // WebSocket 메시지 핸들러에서 Principal로 접근할 수 있도록 설정
                    attributes.put("username", username);
                    attributes.put("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
                    // Principal을 직접 attributes에 저장 (메시지 핸들러에서 사용)
                    attributes.put(Principal.class.getName(), userDetails);

                    log.info("WebSocket 핸드셰이크 성공: username={}", username);
                    return true;
                }
            } catch (Exception e) {
                log.error("WebSocket 핸드셰이크 중 오류 발생", e);
            }
            return false;
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                  WebSocketHandler wsHandler, Exception exception) {
            // 핸드셰이크 완료 후 처리할 로직이 있다면 여기에 작성
        }
    }
}


