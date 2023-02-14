package com.example.demo;

import io.github.vspiliop.schema.test.TestCreated;
import io.github.vspiliop.schema.test.TestEvents;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.commons.codec.Charsets;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static com.example.demo.KafkaMessageVerifier.getSpecificRecordFromJsonWithUnionTypes;
import static com.example.demo.KafkaMessageVerifier.getStringWithUnionTypes;
import static wiremock.org.apache.commons.io.FileUtils.writeStringToFile;

class Stubs {

    public static void aTestCreatedEvent() throws IOException {

        var testCreatedEvent = TestEvents.newBuilder()
                .setCorrelationId(UUID.randomUUID().toString())
                .setPayload(TestCreated
                        .newBuilder()
                        .setTestNumber("first test")
                        .setText("this is a test created event")
                        .build())
                .build();

        writeStringToFile(
                new File("stubs" + File.separator + "testCreatedEvent-schema.json"),
                testCreatedEvent.getSchema().toString(true),
                Charsets.toCharset("UTF-8")
        );

        writeStringToFile(
                new File("stubs" + File.separator + "testCreatedEvent.json"),
                testCreatedEvent.toString(),
                Charsets.toCharset("UTF-8")
        );

        serializeSpecificClassToAvroBytes(testCreatedEvent,
                new File("stubs" + File.separator + "testCreatedEvent.avro"));

        String jsonString = getStringWithUnionTypes(testCreatedEvent);

        writeStringToFile(
                new File("stubs" + File.separator + "testCreatedEvent-full.json"),
                jsonString,
                Charsets.toCharset("UTF-8")
        );

        TestEvents event = getSpecificRecordFromJsonWithUnionTypes(jsonString, TestEvents.class);
        System.out.println(event);
    }

    private static void serializeSpecificClassToAvroBytes(TestEvents testCreatedEvent,
                                                          File file) throws IOException {
        try (var writer = new DataFileWriter<>(new SpecificDatumWriter<>(TestEvents.class))) {
            writer.create(testCreatedEvent.getSchema(), file);
            writer.append(testCreatedEvent);
        }
    }

    public static void main(String... args) throws IOException {
        aTestCreatedEvent();
    }

}
