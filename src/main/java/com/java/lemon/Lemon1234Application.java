package com.java.lemon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(value = "com.java.lemon.mapper")
public class Lemon1234Application {

	public static void main(String[] args) {
		SpringApplication.run(Lemon1234Application.class, args);
	}

}
