package io.github.guilherme_eira.hb_discovery_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class HbDiscoveryServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(HbDiscoveryServerApplication.class, args);
	}

}
