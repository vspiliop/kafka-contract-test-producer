package com.example.demo;

import io.github.vspiliop.schema.test.TestEvents;
import org.apache.avro.Schema;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.verifier.converter.YamlContract;
import org.springframework.cloud.contract.verifier.messaging.MessageVerifierReceiver;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.DefaultKafkaHeaderMapper;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
		classes = { TestConfig.class, KafkaContractTestProducerApplication.class })
@Testcontainers
@AutoConfigureMessageVerifier
@ActiveProfiles("test")
public abstract class BaseClass {

	@Container
	static KafkaContainer kafka
			= new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.1"));

	@DynamicPropertySource
	static void kafkaProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
	}

	@Autowired
	Producer producer;

	/**
	 * This method should trigger the business logic code that produces a proper TestCreatedEvent.
	 * Usually this is a test, that will provide the required inputs to the producer, so that it
	 * produces the proper event (aka TestCreatedEvent in this case).
	 *
	 * If the former input is provided by another service, then a 2nd contract test between that
	 * 3rd service and the producer should be written.
	 */
	public void sendTestCreatedEvent() throws ExecutionException, InterruptedException, TimeoutException {
		this.producer.sendTestCreatedEvent();
	}

	/**
	 * This method should trigger the business logic code that produces a proper TestUpdatedEvent.
	 * Usually this is a test, that will provide the required inputs to the producer, so that it
	 * produces the proper event (aka TestUpdatedEvent in this case).
	 *
	 * If the former input is provided by another service, then a 2nd contract test between that
	 * 3rd service and the producer should be written.
	 */
	public void sendTestUpdatedEvent() throws ExecutionException, InterruptedException, TimeoutException {
		this.producer.sendTestUpdatedEvent();
	}
}

@EnableKafka
@Configuration
class TestConfig {

	@Bean
	KafkaMessageVerifier kafkaTemplateMessageVerifier() {
		return new KafkaMessageVerifier();
	}
}

class KafkaMessageVerifier implements MessageVerifierReceiver<Message<?>> {

	private static final Logger log = LoggerFactory.getLogger(KafkaMessageVerifier.class);

	private final Map<String, Message> broker = new ConcurrentHashMap<>();

	private final CyclicBarrier cyclicBarrier = new CyclicBarrier(2);

	@Override
	public Message receive(String destination, long timeout, TimeUnit timeUnit, @Nullable YamlContract contract) {
		Message message = message(destination);
		if (message != null) {
			return message;
		}
		await(timeout, timeUnit);
		return message(destination);
	}

	private void await(long timeout, TimeUnit timeUnit) {
		try {
			cyclicBarrier.await(timeout, timeUnit);
		} catch (Exception e) {

		}
	}

	private Message message(String destination) {
		Message message = broker.get(destination);
		if (message != null) {
			broker.remove(destination);
			log.info("Removed a message {} from a topic [{}}]", message, destination);
		}
		return message;
	}

	@KafkaListener(id = "listener", topicPattern = ".*")
	public void listen(ConsumerRecord<String, TestEvents> payload,
					   @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) throws BrokenBarrierException, InterruptedException {
		log.info("Got message [{}], from a topic [{}], with headers [{}]",
				payload.value(), topic, payload.headers());
		String message = getStringWithUnionTypes(payload.value());
		Map<String, Object> headers = new HashMap<>();
		new DefaultKafkaHeaderMapper().toHeaders(payload.headers(), headers);
		broker.put(topic, MessageBuilder.createMessage(message, new MessageHeaders(headers)));
		cyclicBarrier.await();
		cyclicBarrier.reset();
	}

	static <T extends SpecificRecord> String getStringWithUnionTypes(T avroRecord) {
		String jsonString = null;

		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			JsonEncoder encoder = EncoderFactory.get().jsonEncoder(avroRecord.getSchema(), os);
			DatumWriter<T> avroInJsonWithTypesWriter = new SpecificDatumWriter<>();
			avroInJsonWithTypesWriter.setSchema(avroRecord.getSchema());
			avroInJsonWithTypesWriter.write(avroRecord, encoder);
			encoder.flush();
			jsonString = new String(os.toByteArray(), StandardCharsets.UTF_8);
		} catch (IOException e) {}
		return jsonString;
	}

	static <T extends SpecificRecord> T getSpecificRecordFromJsonWithUnionTypes(String JsonWithUnionTypes,
																				Class<T> specificRecord) {
		DatumReader<T> reader = new SpecificDatumReader<>(specificRecord);
		try {
			Decoder decoder = DecoderFactory.get().jsonDecoder(
					(Schema) specificRecord.getField("SCHEMA$").get(null), JsonWithUnionTypes);
			return reader.read(null, decoder);
		} catch (IOException | NoSuchFieldException | IllegalAccessException e) {
			return null;
		}
	}

	@Override
	public Message receive(String destination, YamlContract contract) {
		return receive(destination, 20, TimeUnit.SECONDS, contract);
	}
}
