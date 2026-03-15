package com.phonebid.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.phonebid.app.common.config.PortOneV2Properties;
import com.phonebid.app.notification.config.AligoProperties;

@SpringBootApplication
@EnableConfigurationProperties({PortOneV2Properties.class, AligoProperties.class})
@EnableScheduling
public class PhonebidApplication {

	public static void main(String[] args) {
		SpringApplication.run(PhonebidApplication.class, args);
	}

}
