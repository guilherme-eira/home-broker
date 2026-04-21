package io.github.guilherme_eira.hb_auth_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class HbAuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(HbAuthServiceApplication.class, args);
	}

}
