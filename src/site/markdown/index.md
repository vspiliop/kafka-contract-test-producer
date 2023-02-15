## Contract testing for Kafka event based services

## Table of Contents
<!-- TOC -->
  1. [Key takeaway](#Key-takeaway)
  2. [Why?](#Why)
  3. [The bigger picture](#The-bigger-picture)
  4. [Effective communication requirements](#Effective-communication-requirements)
  5. [Component tests](#Component-tests)
      - [Consumer](#Consumer)
      - [Producer](#Producer)
  6. [Contract tests to the rescue](#Contract-tests-to-the-rescue)
      - [Producer](#Producer-1)
      - [Consumer](#Consumer-1)
  7. [Show me the code](#Show-me-the-code)
  8. [What's next?](#Whats-next)
<!-- TOC -->

| Disclaimer                      |
|---------------------------------|
| Opinionated content follows :-) | 

## Key takeaway

.. and target of this presentation is try to persuade you that:

`Contracts tests are a promising and effective way to reduce end-to-end tests.`

## Why

.. reduce the number of end-to-end tests?

  - Slow
  - Brittle
  - Expensive to set up and maintain
  - We cannot do exhaustive testing
  - Slow feedback
    - No build time feedback
  - Which team owns the end-to-end test?
    - writing
    - fixing
    - maintaining

## The bigger picture

Contract tests lay between end-to-end and component tests.

![](images/src/main/plantuml/testing-pyramid.png)

## Effective communication requirements

Focusing on Instrument and Streams services.

1. Streams can de-serialise the event/ bytes, serialised by Instruments
2. The de-serialised event contains all necessary data for Streams to perform its business logic (has the proper semantics)

- (1) and (2) though related, are not the same!
- A consumer able to de-serialise, does not mean it can deliver its business purpose
  - Avro required fields capture what is required from an event modelling perspective, not the consumer
  - One contract `per consumer` or `per consumer, per consumer use case` would be better?
    - Different consumers may require different fields
    - Different consumers may require different values of the same Enum field
  - Doing compatible avro schema changes usually leads to reducing avro required fields

![](images/src/main/plantuml/producer-consumer.png)

## Component tests

Currently, we do a lot of them.

#### Consumer

![](images/src/main/plantuml/consumer-component-test.png)

Pros:
- Exist on the Streams (consumer) bitbucket repo
- Run as part of the build process
- Fast
- Stable
- Isolated
- Easy to set up

Cons:
- Test mocks the actual producer
- Stall mocks issues!
  - Instruments (i.e. actual runtime producer) moves to a new esp-kafka-schemas version
  - Instruments service removes an avro optional field that is required from the Streams (i.e. consumer) perspective

#### Producer

![](images/src/main/plantuml/producer-component-test.png)

Pros:
- Exist on the Instrument (producer) bitbucket repo
- Run as part of the build process
- Fast
- Stable
- Isolated
- Easy to set up

Cons:
- How does the producer know that it emits events that are actually what the consumer expects? I does not.
  - Streams requires a particular value of an enum field (e.g. `status` to be `FULFILLED`), but the producer never generates a `FULFILLED` event?

## Contract tests to the rescue

#### <a id="Producer-1"></a> Producer

![](images/src/main/plantuml/producer-contract-test.png)

Pros:
- Exist on the Instrument (producer) bitbucket repo
- Run as part of the build process
- Fast
- Stable
- Isolated
- Easy to set up

2 extras:
- If producer by mistake stops following the contract producer build fails!
- Meaning, the producer emits events that the consumers actually expect

#### <a id="Consumer-1"></a> Consumer

![](images/src/main/plantuml/consumer-contract-test.png)

Pros:
- Exist on the Instrument (producer) bitbucket repo
- Run as part of the build process
- Fast
- Stable
- Isolated
- Easy to set up

and an extra one:
- No stall mocks issue!

## Show me the code

Open Intellij.. 

## What's next?

- Should the producer have a contract per consumer's use case scenario?
  - The consuming service uses the sample event as is for testing these use cases
- Maybe contract tests should focus on verifying just event structure
- Is there any benefit of using avro, regarding schema evolution, if you do contract testing?
- Someone has to do a POC of the whole dev process: from Pull Request to DVA and beyond..
- What about non JVM producers/ consumer?
  - OTPm is a C++ Kafka producer
  - MMC UI is a Nodejs REST consumer