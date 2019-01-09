package com.ddcrawler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@TestPropertySource(properties = "app.scheduling.enable=false")
@SpringBootTest(classes = {DdCrawlerApplication.class})
public class DdCrawlerApplicationTests {

	@Test
	public void contextLoads() {

	}

}
