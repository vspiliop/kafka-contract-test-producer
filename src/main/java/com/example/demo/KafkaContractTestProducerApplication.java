package com.example.demo;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class KafkaContractTestProducerApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaContractTestProducerApplication.class, args);
	}

	@Bean
	public NewTopic topic() {
		return new NewTopic("topic1", 1, (short) 1);
	}

}
