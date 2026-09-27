package com.YM.Beverage.Distribution.Backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.TimeZone;

@SpringBootApplication
public class BeverageDistributionBackendApplication {
	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Africa/Addis_Ababa"));
		SpringApplication.run(BeverageDistributionBackendApplication.class, args);
	}
	@Bean
	public RestTemplate restTemplate(){
		return new RestTemplate();
	}

}
