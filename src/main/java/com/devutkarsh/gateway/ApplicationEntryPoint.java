package com.devutkarsh.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@EnableZuulProxy
@SpringBootApplication
public class ApplicationEntryPoint {

	public static void main(String[] args) {
		SpringApplication.run(ApplicationEntryPoint.class, args);
	}



}