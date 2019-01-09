package com.ddcrawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
//@EnableScheduling
@EnableAsync
public class DdCrawlerApplication extends SpringBootServletInitializer {
	private final Logger log = LoggerFactory.getLogger(DdCrawlerApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(DdCrawlerApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		// 注意这里要指向原先用main方法执行的Application启动类
		return builder.sources(DdCrawlerApplication.class);
	}

	@RestController
	public static class ExampleApplicationController{

		@GetMapping("/")
		public String handler(){
			return "Hello";
		}
	}
}
