package com.github.richygreat.springsagaevent;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.richygreat.springsagaevent.annotation.EnableSagaEvents;
import com.github.richygreat.springsagaevent.service.CreateTransferSagaService;

@SpringBootTest(classes = AppTest.TestConfig.class)
@ExtendWith(SpringExtension.class)
class AppTest {
	@Autowired
	private CreateTransferSagaService testService;

	@Test
	void testOrderCreation() throws InterruptedException, IOException {
		testService.initTransfer();
		Thread.sleep(15000);
	}

	@Configuration
	@ComponentScan
	@EnableSagaEvents
	@EnableAutoConfiguration
	static class TestConfig {
	}
}
