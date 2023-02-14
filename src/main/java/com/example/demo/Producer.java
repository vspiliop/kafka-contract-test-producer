package com.example.demo;

import io.github.vspiliop.schema.test.TestCreated;
import io.github.vspiliop.schema.test.TestEvents;
import io.github.vspiliop.schema.test.TestUpdated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


@Slf4j
@Service
public class Producer {

	@Autowired
	private KafkaTemplate<String, TestEvents> template;

	public void sendTestCreatedEvent() throws ExecutionException, InterruptedException, TimeoutException {
		log.info("Sending TestCreate event..");

		TestEvents event2Send = TestEvents.newBuilder()
				.setType("TestCreated")
				.setCorrelationId(UUID.randomUUID().toString())
				.setPayload(TestCreated
						.newBuilder()
						.setTestNumber("first test")
						.setText("this is a test created event")
						.build())
				.build();

		SendResult<String, TestEvents> result =
				this.template.send("topic1", event2Send).get(5, TimeUnit.SECONDS);
		log.info("Event sent: {}", result);
	}

	public void sendTestUpdatedEvent() throws ExecutionException, InterruptedException, TimeoutException {
		log.info("Sending TestUpdated event..");

		TestEvents event2Send = TestEvents.newBuilder()
				.setCorrelationId(UUID.randomUUID().toString())
				.setPayload(TestUpdated
						.newBuilder()
						.setText("this is a test updated event")
						.build())
				.build();

		SendResult<String, TestEvents> result =
				this.template.send("topic1", event2Send).get(5, TimeUnit.SECONDS);
		log.info("Event sent: {}", result);
	}

}
