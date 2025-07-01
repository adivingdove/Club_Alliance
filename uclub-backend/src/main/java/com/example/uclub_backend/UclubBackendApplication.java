package com.example.uclub_backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@SpringBootApplication

@MapperScan({"com.example.uclub_backend.mapper", "com.example.uclub_backend.forum.mapper"})

public class UclubBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(UclubBackendApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(5000); // 5秒连接超时
		factory.setReadTimeout(120000);   // 120秒读取超时
		return new RestTemplate(factory);
	}

}
