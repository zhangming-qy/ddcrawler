package com.ddcrawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
//@EnableScheduling
@EnableAsync
public class DdCrawlerApplication extends SpringBootServletInitializer {
	private final Logger log = LoggerFactory.getLogger(DdCrawlerApplication.class);
	private static ConfigurableApplicationContext context;

	public static void main(String[] args) {
		SpringApplication.run(DdCrawlerApplication.class, args);
	}

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		// 注意这里要指向原先用main方法执行的Application启动类
		return builder.sources(DdCrawlerApplication.class);
	}

	public static void restart() {
		ApplicationArguments args = context.getBean(ApplicationArguments.class);

		Thread thread = new Thread(() -> {
			context.close();
			context = SpringApplication.run(DdCrawlerApplication.class, args.getSourceArgs());
		});

		thread.setDaemon(false);
		thread.start();
	}

	@RestController
	public static class DdCrawlerApplicationController{

		@GetMapping("/")
		public String handler(){
			return "Hello";
		}

		@RequestMapping("/{op}")
		public String restart(@PathVariable String op){
			if(op.equals("restart")){
				DdCrawlerApplication.restart();
				return "Restart application successfully!";
			}

			return "";
		}
	}
}
