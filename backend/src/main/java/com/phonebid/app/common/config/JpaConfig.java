package com.phonebid.app.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;

@Configuration
@EnableJpaAuditing // jpa 감사 기능 활성화
public class JpaConfig {
    
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditAwareImpl();
    }

}
