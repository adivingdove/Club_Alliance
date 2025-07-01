package com.example.uclub_backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({"com.example.uclub_backend.mapper", "com.example.uclub_backend.forum.mapper"})
public class UclubBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(UclubBackendApplication.class, args);
	}

}
