# Java Ethereum Beacon Chain Events Listener

A Java library for listening to Ethereum Beacon Chain events via Server-Sent Events (SSE).

## Overview

This library provides a simple and efficient way to subscribe to and receive events from the Ethereum Beacon Chain. It uses the Server-Sent Events (SSE) API provided by Ethereum nodes to receive real-time updates about various events on the blockchain.

The library is designed to be:

- **Independent of web3j**: Built from scratch without dependencies on web3j.
- **Efficient**: Uses OkHttp's SSE client for efficient event streaming.
- **Easy to use**: Provides a simple, fluent API for subscribing to events.
- **Flexible**: Supports all event types provided by the Ethereum Beacon Chain API.
- **Robust**: Handles connection failures and automatically reconnects.
- **Generic**: Uses a generic approach with raw JSON data, allowing access to all event fields without requiring specific event classes for each event type.

## Supported Event Types

The library supports all event types provided by the Ethereum Beacon Chain API:

- `head`: Sent when a new head is added to the canonical chain.
- `block`: Sent when a new block is received.
- `block_gossip`: Sent when a new block is received via gossip.
- `attestation`: Sent when a new attestation is received.
- `single_attestation`: Sent when a new attestation is received for a specific validator.
- `voluntary_exit`: Sent when a new voluntary exit is received.
- `bls_to_execution_change`: Sent when a new BLS to execution change is received.
- `proposer_slashing`: Sent when a new proposer slashing is received.
- `attester_slashing`: Sent when a new attester slashing is received.
- `finalized_checkpoint`: Sent when a new finalized checkpoint is received.
- `chain_reorg`: Sent when a chain reorganization occurs.
- `contribution_and_proof`: Sent when a new contribution and proof is received.
- `light_client_finality_update`: Sent when a new light client finality update is received.
- `light_client_optimistic_update`: Sent when a new light client optimistic update is received.
- `payload_attributes`: Sent when new payload attributes are received.
- `blob_sidecar`: Sent when a new blob sidecar is received.

## Installation

Add the following dependency to your Maven project:

```xml
<dependency>
    <groupId>dev.amilcar</groupId>
    <artifactId>java-eth-events-listener</artifactId>
    <version>1.0</version>
</dependency>
```

## Usage

### Basic Usage

```java
// Create a client for head eventslistener only
BeaconEventClient client = BeaconEventClientFactory.createHeadEventClient("http://localhost:5051");

// Add a listener for head eventslistener
client.addEventListener(EventType.HEAD, (BeaconEvent event) -> {
    System.out.println("Received head event: " + event);
    System.out.println("Slot: " + event.getData().get("slot").asText());
    System.out.println("Block: " + event.getData().get("block").asText());
});

// Start the client
client.start();

// When done, close the client
client.close();
```

### Subscribing to Multiple Event Types

```java
// Create a client for multiple event types
BeaconEventClient client = BeaconEventClientFactory.createClient(
        "http://localhost:5051",
        Set.of(EventType.HEAD, EventType.BLOCK, EventType.FINALIZED_CHECKPOINT)
);

// Add listeners for different event types
client.addEventListener(EventType.HEAD, (BeaconEvent event) -> {
    System.out.println("Received head event: " + event);
});

// Start the client
client.start();
```

### Using the Builder

For more control over the client configuration, you can use the builder directly:

```java
BeaconEventClient client = new BeaconEventClientBuilder()
        .nodeUrl("http://localhost:5051")
        .addEventType(EventType.HEAD)
        .addEventType(EventType.BLOCK)
        .build();

client.addEventListener(EventType.HEAD, (BeaconEvent event) -> {
    System.out.println("Received head event: " + event);
});

client.start();
```

### Waiting for Events

You can use a CountDownLatch to wait for events:

```java
CountDownLatch latch = new CountDownLatch(1);

BeaconEventClient client = BeaconEventClientFactory.createHeadEventClient("http://localhost:5051");

client.addEventListener(EventType.HEAD, (BeaconEvent event) -> {
    System.out.println("Received head event: " + event);
    latch.countDown();
});

client.start();

// Wait for one event or timeout after 60 seconds
boolean received = latch.await(60, TimeUnit.SECONDS);

if (received) {
    System.out.println("Successfully received an event!");
} else {
    System.out.println("Timed out waiting for an event");
}

client.close();
```

## Working with Events

All events in this library are represented by the `BeaconEvent` class, which contains the event type and the raw JSON data as a Jackson `JsonNode`. To access the data in an event, use the `getData()` method and then use the JsonNode API to navigate the data structure:

```java
client.addEventListener(EventType.HEAD, (BeaconEvent event) -> {
    // Get the event type
    EventType type = event.getEventType();

    // Get the raw JSON data
    JsonNode data = event.getData();

    // Access fields in the data
    String slot = data.get("slot").asText();
    String block = data.get("block").asText();

    // Check if a field exists
    if (data.has("epoch")) {
        String epoch = data.get("epoch").asText();
    }

    // Access nested fields
    if (data.has("previous_duty_dependent_root")) {
        String root = data.get("previous_duty_dependent_root").asText();
    }
});
```

The structure of the data depends on the event type. Refer to the Ethereum Beacon Chain API documentation for details on the data structure for each event type.

## Advanced Configuration

The library provides several ways to customize the client:

### Custom HTTP Client

```java
OkHttpClient httpClient = new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build();

BeaconEventClient client = new BeaconEventClientBuilder()
        .nodeUrl("http://localhost:5051")
        .httpClient(httpClient)
        .addEventType(EventType.HEAD)
        .build();
```

### Custom JSON Mapper

```java
ObjectMapper objectMapper = new ObjectMapper();
// Configure the mapper as needed

BeaconEventClient client = new BeaconEventClientBuilder()
        .nodeUrl("http://localhost:5051")
        .objectMapper(objectMapper)
        .addEventType(EventType.HEAD)
        .build();
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
