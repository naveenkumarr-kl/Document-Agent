package com.example.capstone_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableWebSecurity
public class CapstoneProjectApplication{

	public static void main(String[] args) {

        SpringApplication.run(CapstoneProjectApplication.class, args);
	}

}
