package io.github.amilcar.eth.eventslistener.client;

import io.github.amilcar.eth.eventslistener.model.EventType;

import java.util.Set;

/**
 * Factory for creating instances of {@link BeaconEventClient} with common configurations.
 * This class provides convenient methods for creating clients without having to use the builder directly.
 */
public class BeaconEventClientFactory {
    
    private BeaconEventClientFactory() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * Creates a new {@link BeaconEventClient} for the specified node URL.
     *
     * @param nodeUrl the URL of the Ethereum node
     * @return a new {@link BeaconEventClient} instance
     */
    public static BeaconEventClient createClient(String nodeUrl) {
        return new BeaconEventClientBuilder()
                .nodeUrl(nodeUrl)
                .build();
    }
    
    /**
     * Creates a new {@link BeaconEventClient} for the specified node URL and event type.
     *
     * @param nodeUrl the URL of the Ethereum node
     * @param eventType the event type to subscribe to
     * @return a new {@link BeaconEventClient} instance
     */
    public static BeaconEventClient createClient(String nodeUrl, EventType eventType) {
        return new BeaconEventClientBuilder()
                .nodeUrl(nodeUrl)
                .addEventType(eventType)
                .build();
    }
    
    /**
     * Creates a new {@link BeaconEventClient} for the specified node URL and event types.
     *
     * @param nodeUrl the URL of the Ethereum node
     * @param eventTypes the event types to subscribe to
     * @return a new {@link BeaconEventClient} instance
     */
    public static BeaconEventClient createClient(String nodeUrl, Set<EventType> eventTypes) {
        return new BeaconEventClientBuilder()
                .nodeUrl(nodeUrl)
                .addEventTypes(eventTypes)
                .build();
    }
    
    /**
     * Creates a new {@link BeaconEventClient} for the specified node URL and all event types.
     *
     * @param nodeUrl the URL of the Ethereum node
     * @return a new {@link BeaconEventClient} instance
     */
    public static BeaconEventClient createClientForAllEvents(String nodeUrl) {
        return new BeaconEventClientBuilder()
                .nodeUrl(nodeUrl)
                .addEventTypes(Set.of(EventType.values()))
                .build();
    }
    
    /**
     * Creates a new {@link BeaconEventClient} for the specified node URL and head eventslistener only.
     *
     * @param nodeUrl the URL of the Ethereum node
     * @return a new {@link BeaconEventClient} instance
     */
    public static BeaconEventClient createHeadEventClient(String nodeUrl) {
        return createClient(nodeUrl, EventType.HEAD);
    }
    
    /**
     * Creates a new {@link BeaconEventClient} for the specified node URL and block eventslistener only.
     *
     * @param nodeUrl the URL of the Ethereum node
     * @return a new {@link BeaconEventClient} instance
     */
    public static BeaconEventClient createBlockEventClient(String nodeUrl) {
        return createClient(nodeUrl, EventType.BLOCK);
    }
    
    /**
     * Creates a new {@link BeaconEventClient} for the specified node URL and finalized checkpoint eventslistener only.
     *
     * @param nodeUrl the URL of the Ethereum node
     * @return a new {@link BeaconEventClient} instance
     */
    public static BeaconEventClient createFinalizedCheckpointEventClient(String nodeUrl) {
        return createClient(nodeUrl, EventType.FINALIZED_CHECKPOINT);
    }
    
    /**
     * Creates a new {@link BeaconEventClient} for the specified node URL and chain reorganization eventslistener only.
     *
     * @param nodeUrl the URL of the Ethereum node
     * @return a new {@link BeaconEventClient} instance
     */
    public static BeaconEventClient createChainReorgEventClient(String nodeUrl) {
        return createClient(nodeUrl, EventType.CHAIN_REORG);
    }
}