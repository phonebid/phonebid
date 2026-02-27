package com.phonebid.app.notification.config;

import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

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
     */
    @Bean("aligoWebClient")
    public WebClient aligoWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 
                       (int) aligoProperties.getTimeout().getConnect().toMillis())
                .responseTimeout(aligoProperties.getTimeout().getRead());
        
        return WebClient.builder()
                .baseUrl(aligoProperties.getApi().getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
