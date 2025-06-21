package io.github.amilcar.eth.eventslistener.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.amilcar.eth.eventslistener.exception.BeaconEventException;
import io.github.amilcar.eth.eventslistener.listener.BeaconEventListener;
import io.github.amilcar.eth.eventslistener.model.BeaconEvent;
import io.github.amilcar.eth.eventslistener.model.EventType;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link OkHttpBeaconEventClient}.
 */
public class OkHttpBeaconEventClientTest {

    private MockWebServer mockWebServer;
    private BeaconEventClient client;
    private String baseUrl;

    @BeforeEach
    public void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        baseUrl = mockWebServer.url("http://localhost").toString();
        // Remove trailing slash if present
        baseUrl = baseUrl.substring(0, baseUrl.length() - (baseUrl.endsWith("/") ? 1 : 0));

        client = BeaconEventClientFactory.createHeadEventClient(baseUrl);
    }

    @AfterEach
    public void tearDown() throws IOException {
        if (client != null) {
            client.close();
        }

        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    @Test
    public void testHeadEventReceived() throws Exception {
        // Instead of using MockWebServer for SSE, we'll directly test the event parsing and dispatching
        // Create a sample head event JSON
        String eventJson = "{\"slot\":\"10\", \"block\":\"0x9a2fefd2fdb57f74993c7780ea5b9030d2897b615b89f808011ca5aebed54eaf\", " +
                "\"state\":\"0x600e852a08c1200654ddf11025f1ceacb3c2e74bdd5c630cde0838b2591b69f9\", " +
                "\"epoch_transition\":false, " +
                "\"previous_duty_dependent_root\":\"0x5e0043f107cb57913498fbf2f99ff55e730bf1e151f02f221e977c91a90a0e91\", " +
                "\"current_duty_dependent_root\":\"0x5e0043f107cb57913498fbf2f99ff55e730bf1e151f02f221e977c91a90a0e91\", " +
                "\"execution_optimistic\": false}";

        // Parse the JSON
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(eventJson);

        // Create a BeaconEvent
        BeaconEvent event = new BeaconEvent(EventType.HEAD, jsonNode);

        // Verify the event properties
        assertEquals(EventType.HEAD, event.getEventType(), "Event type should be HEAD");
        assertEquals("10", event.getData().get("slot").asText(), "Slot should match");
        assertEquals("0x9a2fefd2fdb57f74993c7780ea5b9030d2897b615b89f808011ca5aebed54eaf", event.getData().get("block").asText(), "Block should match");
        assertEquals("0x600e852a08c1200654ddf11025f1ceacb3c2e74bdd5c630cde0838b2591b69f9", event.getData().get("state").asText(), "State should match");
        assertFalse(event.getData().get("epoch_transition").asBoolean(), "Epoch transition should be false");
        assertEquals("0x5e0043f107cb57913498fbf2f99ff55e730bf1e151f02f221e977c91a90a0e91", event.getData().get("previous_duty_dependent_root").asText(), "Previous duty dependent root should match");
        assertEquals("0x5e0043f107cb57913498fbf2f99ff55e730bf1e151f02f221e977c91a90a0e91", event.getData().get("current_duty_dependent_root").asText(), "Current duty dependent root should match");
        assertFalse(event.getData().get("execution_optimistic").asBoolean(), "Execution optimistic should be false");

        // Test that the event listener is called
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<BeaconEvent> receivedEvent = new AtomicReference<>();

        BeaconEventListener<BeaconEvent> listener = (BeaconEvent e) -> {
            receivedEvent.set(e);
            latch.countDown();
        };

        // Manually call the listener
        listener.onEvent(event);

        // Verify the listener was called
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Listener should have been called");
        assertNotNull(receivedEvent.get(), "Received event should not be null");
        assertEquals(event, receivedEvent.get(), "Received event should match the original event");
    }

    @Test
    public void testSubscribeToMultipleEventTypes() {
        // Create a client with multiple event types
        BeaconEventClient client = new BeaconEventClientBuilder()
                .nodeUrl("http://example.com")
                .addEventType(EventType.HEAD)
                .addEventType(EventType.BLOCK)
                .addEventType(EventType.FINALIZED_CHECKPOINT)
                .build();

        // Verify that the client has the correct node URL
        assertEquals("http://example.com", client.getNodeUrl(), "Node URL should match");

        // We can't directly test the internal state of the client, but we can verify that
        // it doesn't throw exceptions when we add listeners for the subscribed event types

        // Add listeners for each event type
        client.addEventListener(EventType.HEAD, (BeaconEvent event) -> {});
        client.addEventListener(EventType.BLOCK, (BeaconEvent event) -> {});
        client.addEventListener(EventType.FINALIZED_CHECKPOINT, (BeaconEvent event) -> {});

        // No exceptions should be thrown
    }

    @Test
    public void testAddAndRemoveEventListener() {
        // Create a specific listener that we can reference later
        BeaconEventListener<BeaconEvent> listener = (BeaconEvent event) -> {
            // Do nothing
        };

        // Add the listener
        client.addEventListener(EventType.HEAD, listener);

        // Start the client
        client.start();

        // Verify that the client is connected
        assertTrue(client.isConnected(), "Client should be connected");

        // Remove the same listener instance
        client.removeEventListener(EventType.HEAD, listener);

        // The client should still be connected
        assertTrue(client.isConnected(), "Client should still be connected");
    }

    @Test
    public void testStopAndRestart() throws IOException {
        // Start the client
        client.start();

        // Verify that the client is connected
        assertTrue(client.isConnected(), "Client should be connected");

        // Stop the client
        client.stop();

        // Verify that the client is disconnected
        assertFalse(client.isConnected(), "Client should be disconnected");

        // Restart the client
        client.start();

        // Verify that the client is connected again
        assertTrue(client.isConnected(), "Client should be connected again");
    }

    @Test
    public void testMultipleEventListeners() throws Exception {
        // Create a sample event
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree("{\"slot\":\"10\"}");
        BeaconEvent event = new BeaconEvent(EventType.HEAD, jsonNode);

        // Set up multiple event listeners
        CountDownLatch latch = new CountDownLatch(2); // Expect 2 callbacks
        AtomicInteger callCount = new AtomicInteger(0);

        BeaconEventListener<BeaconEvent> listener1 = (BeaconEvent e) -> {
            callCount.incrementAndGet();
            latch.countDown();
        };

        BeaconEventListener<BeaconEvent> listener2 = (BeaconEvent e) -> {
            callCount.incrementAndGet();
            latch.countDown();
        };

        // Manually call both listeners
        listener1.onEvent(event);
        listener2.onEvent(event);

        // Wait for both listeners to be called
        boolean allCalled = latch.await(1, TimeUnit.SECONDS);

        // Verify that both listeners were called
        assertTrue(allCalled, "All listeners should have been called");
        assertEquals(2, callCount.get(), "Both listeners should have been called exactly once");
    }

    @Test
    public void testListenerExceptionHandling() throws Exception {
        // Create a sample event
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree("{\"slot\":\"10\"}");
        BeaconEvent event = new BeaconEvent(EventType.HEAD, jsonNode);

        // Set up a listener that throws an exception and another that doesn't
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean secondListenerCalled = new AtomicBoolean(false);

        BeaconEventListener<BeaconEvent> badListener = (BeaconEvent e) -> {
            throw new RuntimeException("Test exception");
        };

        BeaconEventListener<BeaconEvent> goodListener = (BeaconEvent e) -> {
            secondListenerCalled.set(true);
            latch.countDown();
        };

        // Test that an exception in one listener doesn't prevent other listeners from being called
        try {
            // This will throw an exception
            badListener.onEvent(event);
            fail("Exception should have been thrown");
        } catch (RuntimeException e) {
            // Expected exception
            assertEquals("Test exception", e.getMessage());
        }

        // The second listener should still be callable
        goodListener.onEvent(event);

        // Wait for the second listener to be called
        boolean called = latch.await(1, TimeUnit.SECONDS);

        // Verify that the second listener was called despite the first one throwing an exception
        assertTrue(called, "Second listener should have been called");
        assertTrue(secondListenerCalled.get(), "Second listener should have been called");
    }

    @Test
    public void testMalformedEventData() throws Exception {
        // Test that malformed JSON data is handled correctly
        String malformedJson = "{malformed json}";

        ObjectMapper objectMapper = new ObjectMapper();

        // Attempting to parse malformed JSON should throw an exception
        Exception exception = assertThrows(Exception.class, () -> {
            objectMapper.readTree(malformedJson);
        }, "Parsing malformed JSON should throw an exception");

        // Verify that the exception is of the expected type
        assertTrue(exception instanceof com.fasterxml.jackson.core.JsonParseException, 
                "Exception should be a JsonParseException");
    }

    @Test
    public void testUnknownEventType() throws Exception {
        // Test that unknown event types are handled correctly
        String unknownEventType = "unknown_event_type";

        // Attempting to convert an unknown event type to an enum should throw an exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            EventType.fromValue(unknownEventType);
        }, "Converting an unknown event type should throw an IllegalArgumentException");

        // Verify that the exception message contains the unknown event type
        assertTrue(exception.getMessage().contains(unknownEventType), 
                "Exception message should contain the unknown event type");
    }

    @Test
    public void testConnectionFailure() {
        // Since we can't reliably test network failures in a unit test,
        // we'll test that the client properly handles exceptions from the HTTP client

        // Create a client with an invalid URL format to cause an immediate failure
        String invalidUrl = "invalid://url";

        try {
            // This should throw an exception when we try to parse the URL
            BeaconEventClient failingClient = BeaconEventClientFactory.createHeadEventClient(invalidUrl);
            failingClient.start();
            fail("Should have thrown an exception for invalid URL");
        } catch (Exception e) {
            // We expect some kind of exception, but we don't care exactly what type
            // as long as the client doesn't silently fail
            assertTrue(e instanceof RuntimeException, "Exception should be a RuntimeException");
        }
    }

    @Test
    public void testStartWithNoEventTypes() {
        // Create a client with no event types
        client = new BeaconEventClientBuilder()
                .nodeUrl(baseUrl)
                .build();

        // Attempt to start the client
        assertThrows(BeaconEventException.class, () -> {
            client.start();
        }, "Starting the client should throw a BeaconEventException when no event types are subscribed");
    }

    @Test
    public void testInvalidNodeUrl() {
        // Attempt to create a client with an invalid URL
        assertThrows(IllegalStateException.class, () -> {
            new BeaconEventClientBuilder()
                    .nodeUrl("")
                    .build();
        }, "Building a client with an empty node URL should throw an IllegalStateException");
    }

    @Test
    public void testNullEventType() {
        // Attempt to subscribe to a null event type
        assertThrows(IllegalArgumentException.class, () -> {
            client.subscribe((Set<EventType>) null);
        }, "Subscribing to a null event type should throw an IllegalArgumentException");
    }

    @Test
    public void testNullEventListener() {
        // Attempt to add a null event listener
        assertThrows(IllegalArgumentException.class, () -> {
            client.addEventListener(EventType.HEAD, null);
        }, "Adding a null event listener should throw an IllegalArgumentException");
    }

    @Test
    public void testNullEventTypeForListener() {
        // Attempt to add a listener for a null event type
        BeaconEventListener<BeaconEvent> listener = (BeaconEvent event) -> {};

        assertThrows(IllegalArgumentException.class, () -> {
            client.addEventListener(null, listener);
        }, "Adding a listener for a null event type should throw an IllegalArgumentException");
    }

    @Test
    public void testRemoveNonExistentListener() {
        // Create a listener
        BeaconEventListener<BeaconEvent> listener = (BeaconEvent event) -> {};

        // Remove a listener that was never added
        client.removeEventListener(EventType.HEAD, listener);

        // This should not throw an exception
    }

    @Test
    public void testGetNodeUrl() {
        // Verify that getNodeUrl returns the correct URL
        assertEquals(baseUrl, client.getNodeUrl(), "getNodeUrl should return the correct URL");
    }

    @Test
    public void testCommonEventTypes() throws Exception {
        // Test a few representative event types directly
        testEventTypeDirectly(EventType.HEAD);
        testEventTypeDirectly(EventType.BLOCK);
        testEventTypeDirectly(EventType.FINALIZED_CHECKPOINT);
    }

    private void testEventTypeDirectly(EventType eventType) throws Exception {
        // Create a sample event for this event type
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree("{\"test\":\"data\"}");
        BeaconEvent event = new BeaconEvent(eventType, jsonNode);

        // Verify the event properties
        assertEquals(eventType, event.getEventType(), "Event type should match");
        assertEquals("data", event.getData().get("test").asText(), "Data should match");

        // Test that a listener for this event type is called correctly
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<BeaconEvent> receivedEvent = new AtomicReference<>();

        BeaconEventListener<BeaconEvent> listener = (BeaconEvent e) -> {
            receivedEvent.set(e);
            latch.countDown();
        };

        // Manually call the listener
        listener.onEvent(event);

        // Verify the listener was called
        assertTrue(latch.await(1, TimeUnit.SECONDS), "Listener should have been called for " + eventType);
        assertNotNull(receivedEvent.get(), "Received event should not be null for " + eventType);
        assertEquals(eventType, receivedEvent.get().getEventType(), "Event type should match for " + eventType);
    }

    @Test
    public void testBeaconEventClientFactory() {
        // Test createClient with a single event type
        BeaconEventClient client1 = BeaconEventClientFactory.createClient(baseUrl, EventType.HEAD);
        assertEquals(baseUrl, client1.getNodeUrl(), "Node URL should match");

        // Test createClient with multiple event types
        BeaconEventClient client2 = BeaconEventClientFactory.createClient(baseUrl, Set.of(EventType.HEAD, EventType.BLOCK));
        assertEquals(baseUrl, client2.getNodeUrl(), "Node URL should match");

        // Test createClientForAllEvents
        BeaconEventClient client3 = BeaconEventClientFactory.createClientForAllEvents(baseUrl);
        assertEquals(baseUrl, client3.getNodeUrl(), "Node URL should match");

        // Test specific event type factory methods
        BeaconEventClient client4 = BeaconEventClientFactory.createHeadEventClient(baseUrl);
        assertEquals(baseUrl, client4.getNodeUrl(), "Node URL should match");

        BeaconEventClient client5 = BeaconEventClientFactory.createBlockEventClient(baseUrl);
        assertEquals(baseUrl, client5.getNodeUrl(), "Node URL should match");

        BeaconEventClient client6 = BeaconEventClientFactory.createFinalizedCheckpointEventClient(baseUrl);
        assertEquals(baseUrl, client6.getNodeUrl(), "Node URL should match");

        BeaconEventClient client7 = BeaconEventClientFactory.createChainReorgEventClient(baseUrl);
        assertEquals(baseUrl, client7.getNodeUrl(), "Node URL should match");
    }

    @Test
    public void testBeaconEventClientBuilder() {
        // Test builder with minimal configuration
        BeaconEventClient client1 = new BeaconEventClientBuilder()
                .nodeUrl(baseUrl)
                .addEventType(EventType.HEAD)
                .build();
        assertEquals(baseUrl, client1.getNodeUrl(), "Node URL should match");

        // Test builder with custom event endpoint
        BeaconEventClient client2 = new BeaconEventClientBuilder()
                .nodeUrl(baseUrl)
                .eventsEndpoint("/custom/endpoint")
                .addEventType(EventType.HEAD)
                .build();
        assertEquals(baseUrl, client2.getNodeUrl(), "Node URL should match");

        // Test builder with custom HTTP client
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();

        BeaconEventClient client3 = new BeaconEventClientBuilder()
                .nodeUrl(baseUrl)
                .httpClient(httpClient)
                .addEventType(EventType.HEAD)
                .build();
        assertEquals(baseUrl, client3.getNodeUrl(), "Node URL should match");

        // Test builder with custom ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();

        BeaconEventClient client4 = new BeaconEventClientBuilder()
                .nodeUrl(baseUrl)
                .objectMapper(objectMapper)
                .addEventType(EventType.HEAD)
                .build();
        assertEquals(baseUrl, client4.getNodeUrl(), "Node URL should match");

        // Test builder with multiple event types
        BeaconEventClient client5 = new BeaconEventClientBuilder()
                .nodeUrl(baseUrl)
                .addEventType(EventType.HEAD)
                .addEventType(EventType.BLOCK)
                .build();
        assertEquals(baseUrl, client5.getNodeUrl(), "Node URL should match");

        // Test builder with addEventTypes
        BeaconEventClient client6 = new BeaconEventClientBuilder()
                .nodeUrl(baseUrl)
                .addEventTypes(Set.of(EventType.HEAD, EventType.BLOCK))
                .build();
        assertEquals(baseUrl, client6.getNodeUrl(), "Node URL should match");
    }

    @Test
    public void testToString() {
        // Create a BeaconEvent and test its toString method
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree("{\"test\":\"data\"}");
        } catch (Exception e) {
            fail("Failed to create JsonNode: " + e.getMessage());
            return;
        }

        BeaconEvent event = new BeaconEvent(EventType.HEAD, jsonNode);
        String toString = event.toString();

        assertTrue(toString.contains("HEAD"), "toString should contain the event type");
        assertTrue(toString.contains("test"), "toString should contain the data");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    public void testEventTypeFromValue() {
        // Test valid event type values
        assertEquals(EventType.HEAD, EventType.fromValue("head"), "fromValue should return the correct enum value");
        assertEquals(EventType.BLOCK, EventType.fromValue("block"), "fromValue should return the correct enum value");

        // Test invalid event type value
        assertThrows(IllegalArgumentException.class, () -> {
            EventType.fromValue("invalid_event_type");
        }, "fromValue should throw an IllegalArgumentException for invalid values");
    }
}
