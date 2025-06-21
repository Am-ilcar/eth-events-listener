package io.github.amilcar.eth.eventslistener.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.amilcar.eth.eventslistener.model.EventType;
import okhttp3.OkHttpClient;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Builder for creating instances of {@link BeaconEventClient}.
 * This class provides a fluent API for configuring and creating clients.
 */
public class BeaconEventClientBuilder {
    
    private String nodeUrl;
    private String eventsEndpoint;
    private OkHttpClient httpClient;
    private ObjectMapper objectMapper;
    private final Set<EventType> eventTypes = new HashSet<>();
    
    /**
     * Creates a new BeaconEventClientBuilder.
     */
    public BeaconEventClientBuilder() {
        // Default values
        this.eventsEndpoint = "/eth/v1/eventslistener";
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Sets the URL of the Ethereum node.
     *
     * @param nodeUrl the URL of the Ethereum node
     * @return this builder instance for method chaining
     */
    public BeaconEventClientBuilder nodeUrl(String nodeUrl) {
        this.nodeUrl = nodeUrl;
        return this;
    }
    
    /**
     * Sets the endpoint for eventslistener.
     *
     * @param eventsEndpoint the endpoint for eventslistener
     * @return this builder instance for method chaining
     */
    public BeaconEventClientBuilder eventsEndpoint(String eventsEndpoint) {
        this.eventsEndpoint = eventsEndpoint;
        return this;
    }
    
    /**
     * Sets the OkHttp client to use.
     *
     * @param httpClient the OkHttp client to use
     * @return this builder instance for method chaining
     */
    public BeaconEventClientBuilder httpClient(OkHttpClient httpClient) {
        this.httpClient = httpClient;
        return this;
    }
    
    /**
     * Sets the Jackson ObjectMapper to use for JSON parsing.
     *
     * @param objectMapper the Jackson ObjectMapper to use
     * @return this builder instance for method chaining
     */
    public BeaconEventClientBuilder objectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }
    
    /**
     * Adds an event type to subscribe to.
     *
     * @param eventType the event type to subscribe to
     * @return this builder instance for method chaining
     */
    public BeaconEventClientBuilder addEventType(EventType eventType) {
        this.eventTypes.add(eventType);
        return this;
    }
    
    /**
     * Adds multiple event types to subscribe to.
     *
     * @param eventTypes the event types to subscribe to
     * @return this builder instance for method chaining
     */
    public BeaconEventClientBuilder addEventTypes(Set<EventType> eventTypes) {
        this.eventTypes.addAll(eventTypes);
        return this;
    }
    
    /**
     * Builds a new {@link BeaconEventClient} with the configured parameters.
     *
     * @return a new {@link BeaconEventClient} instance
     * @throws IllegalStateException if nodeUrl is not set
     */
    public BeaconEventClient build() {
        if (nodeUrl == null || nodeUrl.isEmpty()) {
            throw new IllegalStateException("Node URL must be set");
        }
        
        OkHttpBeaconEventClient client = new OkHttpBeaconEventClient(
                nodeUrl,
                eventsEndpoint,
                httpClient,
                objectMapper
        );
        
        if (!eventTypes.isEmpty()) {
            client.subscribe(eventTypes);
        }
        
        return client;
    }
}