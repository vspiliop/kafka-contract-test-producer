# Contract testing for Kafka event based services

# Table of Contents
<!-- TOC -->
* [Contract testing for Kafka event based services](#Contract-testing-for-kafka-event-based-services)
* [Table of Contents](#Table-of-contents)
* [Key takeaway](#Key-takeaway)
* [Why](#Why)
* [The bigger picture](#The-bigger-picture)
* [Effective communication requirements](#Effective-communication-requirements)
  * [Consumer can read producer data](#Consumer-can-read-producer-data)
  * [Consumer can deliver business value](#Consumer-can-deliver-business-value)
    * [Component tests](#Component-tests)
      * [Consumer](#Consumer)
      * [Producer](#Producer)
    * [Contract tests to the rescue](#Contract-tests-to-the-rescue)
      * [Producer](#Producer-1)
        * [Triggering the producer](#Triggering-the-producer)
      * [Consumer](#Consumer-1)
* [Show me the code](#Show-me-the-code)
* [What's next?](#Whats-next)
<!-- TOC -->

| Disclaimer                      |
|---------------------------------|
| Opinionated content follows :-) | 

# Key takeaway

.. and target of this presentation is try to persuade you that:

`Contracts tests are a promising and effective way to reduce end-to-end tests.`

# Why

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

# The bigger picture

Contract tests sit between end-to-end and component tests.

![](images/src/main/plantuml/testing-pyramid.png)

# Effective communication requirements

Focusing on `RefData` and `Streams` services.

1. Streams can de-serialise the event/ bytes, serialised by RefData
2. The de-serialised event contains all necessary data for Streams to perform its business logic (has the proper semantics)

- (1) and (2) though related, are not the same!
- A consumer able to de-serialise, does not mean it can deliver its business purpose
  - Avro required fields capture what is required from an event modelling perspective, not the consumer
  - One contract `per consumer` or `per consumer, per consumer use case` would be better?
    - Different consumers may require different fields
    - Different consumers may require different values of the same Enum field
  - Doing compatible avro schema changes usually leads to reducing avro required fields

![](images/src/main/plantuml/producer-consumer.png)

## Consumer can read producer data

The avro schemas used by the `Streams Consumer` and `RefData Producer` are compatible, so that `Streams` can de-serialise the bytes,
serialised by `RefData`.

<img src="images/schema-compatibility-matrix.pdf" width="650"/>
<br/>
<img src="images/schema-evolution-matrix.pdf" width="1000"/>

## Consumer can deliver business value

The de-serialised event contains all necessary data for `Streams` to perform its business logic (has the proper semantics).

This can be achieved in two ways:
  - `Components tests`
  - `Contract tests`

### Component tests

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
  - RefData (i.e. actual runtime producer) moves to a new esp-kafka-schemas version
  - RefData service removes an avro optional field that is required from the Streams (i.e. consumer) perspective

#### Producer

![](images/src/main/plantuml/producer-component-test.png)

Pros:
- Exist on the RefData (producer) bitbucket repo
- Run as part of the build process
- Fast
- Stable
- Isolated
- Easy to set up

Cons:
- How does the producer know that it emits events that are actually what the consumer expects? I does not.
  - Streams requires a particular value of an enum field (e.g. `status` to be `FULFILLED`), but the producer never generates a `FULFILLED` event?

### Contract tests to the rescue

#### <a id="Consumer-1"></a> Consumer

![](images/src/main/plantuml/consumer-contract-test.png)

Pros:
- Exist on the RefData (producer) bitbucket repo
- Run as part of the build process
- Fast
- Stable
- Isolated
- Easy to set up

and an extra one:
- No stall mocks issue!

#### <a id="Producer-1"></a> Producer

![](images/src/main/plantuml/producer-contract-test.png)

Pros:
- Exist on the RefData (producer) bitbucket repo
- Run as part of the build process
- Fast
- Stable
- Isolated
- Easy to set up

2 extras:
- If producer by mistake stops following the contract producer build fails!
- Meaning, the producer emits events that the consumers actually expect

##### Triggering the producer

How can we trigger the producer to emit the proper event, so that we verify it against its contract?

> Generate RefData Producer input data as part of the contract test

![](images/src/main/plantuml/contract-refdata-producer-contract-test.png)

Do we have a guarantee:
- that the input data (`c1` and `o1`) used are actually provided during runtime?
- or even that the `DB schema` is correct?

> No! We have to add an extra contract test!

![](images/src/main/plantuml/contract-refdata-producer-contract-test-2.png)

# Show me the code

Open Intellij.. 

# What's next?

- Should the producer have a contract per consumer's use case scenario?
  - The consuming service uses the sample event as is for testing these use cases
- Maybe contract tests should focus on verifying just event structure
- Is there any benefit of using avro, regarding schema evolution, if you do contract testing?
- Someone has to do a POC of the whole dev process: from Pull Request to DVA and beyond..
- What about non JVM producers/ consumer?
  - OTPm is a C++ Kafka producer
  - MMC UI is a Nodejs REST consumer