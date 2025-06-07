package dev.amilcar.eth.events.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.amilcar.eth.events.exception.BeaconEventException;
import dev.amilcar.eth.events.listener.BeaconEventListener;
import dev.amilcar.eth.events.model.BeaconEvent;
import dev.amilcar.eth.events.model.EventType;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Implementation of {@link BeaconEventClient} using OkHttp's SSE client.
 */
public class OkHttpBeaconEventClient implements BeaconEventClient {

    private static final Logger logger = LoggerFactory.getLogger(OkHttpBeaconEventClient.class);
    private static final String DEFAULT_EVENTS_ENDPOINT = "eth/v1/events";

    private final String nodeUrl;
    private final String eventsEndpoint;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Set<EventType> subscribedEventTypes;
    private final Map<EventType, List<BeaconEventListener<BeaconEvent>>> eventListeners;

    private EventSource eventSource;
    private boolean connected;

    /**
     * Creates a new OkHttpBeaconEventClient with the specified parameters.
     *
     * @param nodeUrl the URL of the Ethereum node
     * @param eventsEndpoint the endpoint for events (default is "/eth/v1/events")
     * @param httpClient the OkHttp client to use
     * @param objectMapper the Jackson ObjectMapper to use for JSON parsing
     */
    public OkHttpBeaconEventClient(
            String nodeUrl,
            String eventsEndpoint,
            OkHttpClient httpClient,
            ObjectMapper objectMapper) {
        this.nodeUrl = nodeUrl;
        this.eventsEndpoint = eventsEndpoint != null ? eventsEndpoint : DEFAULT_EVENTS_ENDPOINT;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.subscribedEventTypes = ConcurrentHashMap.newKeySet();
        this.eventListeners = new ConcurrentHashMap<>();
        this.connected = false;
    }

    /**
     * Creates a new OkHttpBeaconEventClient with the specified node URL.
     *
     * @param nodeUrl the URL of the Ethereum node
     */
    public OkHttpBeaconEventClient(String nodeUrl) {
        this(nodeUrl, DEFAULT_EVENTS_ENDPOINT, new OkHttpClient(), new ObjectMapper());
    }

    @Override
    public BeaconEventClient subscribe(Set<EventType> eventTypes) {
        if (eventTypes == null || eventTypes.isEmpty()) {
            throw new IllegalArgumentException("Event types cannot be null or empty");
        }

        subscribedEventTypes.addAll(eventTypes);

        // If already connected, reconnect with the new event types
        if (connected) {
            reconnect();
        }

        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BeaconEvent> BeaconEventClient addEventListener(EventType eventType, BeaconEventListener<T> listener) {
        if (eventType == null) {
            throw new IllegalArgumentException("Event type cannot be null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }

        eventListeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add((BeaconEventListener<BeaconEvent>) listener);

        return this;
    }

    @Override
    public <T extends BeaconEvent> BeaconEventClient removeEventListener(EventType eventType, BeaconEventListener<T> listener) {
        if (eventType == null) {
            throw new IllegalArgumentException("Event type cannot be null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }

        List<BeaconEventListener<BeaconEvent>> listeners = eventListeners.get(eventType);
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                eventListeners.remove(eventType);
            }
        }

        return this;
    }

    @Override
    public BeaconEventClient start() {
        if (connected) {
            return this;
        }

        if (subscribedEventTypes.isEmpty()) {
            throw new BeaconEventException("No event types subscribed. Call subscribe() before start().");
        }

        connect();
        return this;
    }

    @Override
    public BeaconEventClient stop() {
        if (!connected) {
            return this;
        }

        disconnect();
        return this;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public String getNodeUrl() {
        return nodeUrl;
    }

    @Override
    public void close() throws IOException {
        stop();
    }

    private void connect() {
        try {
            String url = buildEventsUrl();
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            EventSourceListener listener = new BeaconEventSourceListener();
            eventSource = EventSources.createFactory(httpClient).newEventSource(request, listener);
            connected = true;

            logger.info("Connected to Ethereum node at {}", url);
        } catch (Exception e) {
            throw new BeaconEventException("Failed to connect to Ethereum node", e);
        }
    }

    private void disconnect() {
        if (eventSource != null) {
            eventSource.cancel();
            eventSource = null;
        }
        connected = false;
        logger.info("Disconnected from Ethereum node");
    }

    private void reconnect() {
        disconnect();
        connect();
    }

    private String buildEventsUrl() {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(nodeUrl + eventsEndpoint).newBuilder();

        // Add topics query parameter with comma-separated event types
        String topics = subscribedEventTypes.stream()
                .map(EventType::getValue)
                .collect(Collectors.joining(","));

        urlBuilder.addQueryParameter("topics", topics);

        return urlBuilder.build().toString();
    }

    private BeaconEvent parseEvent(String eventType, String data) {
        try {
            // Try to find the corresponding EventType enum value
            EventType type = null;
            try {
                type = EventType.fromValue(eventType);
            } catch (IllegalArgumentException e) {
                logger.warn("Unknown event type: {}", eventType);
                return null;
            }

            // Parse the data as a JsonNode
            JsonNode jsonNode = objectMapper.readTree(data);

            // Create a new BeaconEvent with the type and data
            return new BeaconEvent(type, jsonNode);
        } catch (Exception e) {
            logger.error("Failed to parse event data: {}", data, e);
            return null;
        }
    }

    private void dispatchEvent(BeaconEvent event) {
        if (event == null) {
            return;
        }

        EventType eventType = event.getEventType();
        List<BeaconEventListener<BeaconEvent>> listeners = eventListeners.get(eventType);

        if (listeners != null && !listeners.isEmpty()) {
            for (BeaconEventListener<BeaconEvent> listener : listeners) {
                try {
                    listener.onEvent(event);
                } catch (Exception e) {
                    logger.error("Error in event listener", e);
                }
            }
        }
    }

    /**
     * EventSourceListener implementation for handling SSE events.
     */
    private class BeaconEventSourceListener extends EventSourceListener {

        @Override
        public void onOpen(@NotNull EventSource eventSource, Response response) {
            logger.debug("SSE connection opened");
        }

        @Override
        public void onEvent(@NotNull EventSource eventSource, String id, String type, @NotNull String data) {
            logger.debug("Received event: type={}, data={}", type, data);

            BeaconEvent event = parseEvent(type, data);
            if (event != null) {
                dispatchEvent(event);
            }
        }

        @Override
        public void onClosed(@NotNull EventSource eventSource) {
            logger.debug("SSE connection closed");
            connected = false;
        }

        @Override
        public void onFailure(@NotNull EventSource eventSource, Throwable t, Response response) {
            logger.error("SSE connection failure", t);
            connected = false;

            // Attempt to reconnect after a failure
            if (t != null && !(t instanceof IOException && t.getMessage().contains("Canceled"))) {
                logger.info("Attempting to reconnect...");
                try {
                    Thread.sleep(5000); // Wait 5 seconds before reconnecting
                    reconnect();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Reconnection interrupted", e);
                } catch (Exception e) {
                    logger.error("Failed to reconnect", e);
                }
            }
        }
    }
}
