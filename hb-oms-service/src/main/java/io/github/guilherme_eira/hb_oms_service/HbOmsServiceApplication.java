package io.github.guilherme_eira.hb_oms_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class HbOmsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(HbOmsServiceApplication.class, args);
	}

}
