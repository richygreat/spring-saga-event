package com.github.richygreat.springsagaevent;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.richygreat.springsagaevent.annotation.EnableSagaEvents;
import com.github.richygreat.springsagaevent.service.UserService;

@SpringBootTest(classes = TestSaga.TestConfig.class)
@RunWith(SpringRunner.class)
public class TestSaga {
	@Autowired
	private UserService userService;

	@Test
	public void test() throws JsonProcessingException, InterruptedException {
		userService.requestUserCreation();

		Thread.sleep(55000);
	}

	@Configuration
	@ComponentScan
	@EnableSagaEvents
	@EnableAutoConfiguration
	static class TestConfig {
	}
}
