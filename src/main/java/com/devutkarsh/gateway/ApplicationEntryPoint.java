package com.devutkarsh.gateway;

import com.devutkarsh.gateway.fallback.DefaultServiceFallback;
import com.devutkarsh.gateway.filter.RequestFilter;
import com.devutkarsh.gateway.filter.ResponseFilter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;

@EnableZuulProxy
@SpringBootApplication
public class ApplicationEntryPoint {

	public static void main(String[] args) {
		SpringApplication.run(ApplicationEntryPoint.class, args);
	}

	@Bean
	RequestFilter getRequestFilterBean(){
		return new RequestFilter();
	}

	@Bean
	ResponseFilter getResponseFilterBean(){
		return new ResponseFilter();
	}

	@Bean
	DefaultServiceFallback getDefaultServiceFallback(){
		return new DefaultServiceFallback();
	}


}