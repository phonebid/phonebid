package com.phonebid.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.phonebid.app.common.config.PortOneV2Properties;

@SpringBootApplication
@EnableConfigurationProperties(PortOneV2Properties.class)
public class PhonebidApplication {

	public static void main(String[] args) {
		SpringApplication.run(PhonebidApplication.class, args);
	}

}
