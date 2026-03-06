package com.phonebid.app.notification.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.util.concurrent.TimeUnit;

/**
 * 알리고 API 연동을 위한 설정 클래스
 */
@Configuration
@EnableConfigurationProperties(AligoProperties.class)
@RequiredArgsConstructor
public class AligoConfig {
    
    private final AligoProperties aligoProperties;
    
    /**
     * 알리고 API 전용 WebClient 빈 생성
     * Connection Pool과 Timeout 설정이 적용됨
     */
    @Bean("aligoWebClient")
    public WebClient aligoWebClient() {
        AligoProperties.ConnectionPool poolConfig = aligoProperties.getConnectionPool();
        AligoProperties.Timeout timeoutConfig = aligoProperties.getTimeout();
        String baseUrl = aligoProperties.getApi().getBaseUrl();
        
        // Connection Pool 설정
        ConnectionProvider provider = ConnectionProvider.builder("aligo-pool")
                .maxConnections(poolConfig.getMaxConnections())
                .pendingAcquireTimeout(poolConfig.getPendingAcquireTimeout())
                .build();

        // HttpClient 생성 및 타임아웃 설정
        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 
                        (int) timeoutConfig.getConnect().toMillis())
                .responseTimeout(timeoutConfig.getRead())
                .doOnConnected(conn -> 
                    conn.addHandlerLast(new ReadTimeoutHandler(
                            timeoutConfig.getRead().toMillis(), TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(
                            timeoutConfig.getWrite().toMillis(), TimeUnit.MILLISECONDS))
                );

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
