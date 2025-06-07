package dev.amilcar.eth.events.example;

import dev.amilcar.eth.events.client.BeaconEventClient;
import dev.amilcar.eth.events.client.BeaconEventClientFactory;
import dev.amilcar.eth.events.model.BeaconEvent;
import dev.amilcar.eth.events.model.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Example demonstrating how to use the Ethereum Beacon Chain Events Listener library.
 */
public class BeaconEventClientExample {

    private static final Logger logger = LoggerFactory.getLogger(BeaconEventClientExample.class);

    /**
     * Main method demonstrating how to use the library.
     *
     * @param args command line arguments (not used)
     * @throws Exception if an error occurs
     */
    public static void main(String[] args) throws Exception {
        // Replace with your Ethereum node URL
        String nodeUrl = "https://your-ethereum-node-url";

        // Example 1: Create a client for head events only
        logger.info("Creating client for head events only...");
        try (BeaconEventClient headClient = BeaconEventClientFactory.createHeadEventClient(nodeUrl)) {
            // Add a listener for head events
            headClient.addEventListener(EventType.HEAD, (BeaconEvent event) -> {
                logger.info("Received head event: {}", event);
                logger.info("Slot: {}", event.getData().get("slot").asText());
                logger.info("Block: {}", event.getData().get("block").asText());
            });

            // Start the client
            headClient.start();

            // Wait for some events
            Thread.sleep(300);
        }

        // Example 2: Create a client for multiple event types
        logger.info("Creating client for multiple event types...");
        try (BeaconEventClient multiClient = BeaconEventClientFactory.createClient(
                nodeUrl, Set.of(EventType.HEAD, EventType.BLOCK, EventType.FINALIZED_CHECKPOINT))) {

            // Add listeners for different event types
            multiClient.addEventListener(EventType.HEAD, (BeaconEvent event) -> {
                logger.info("Received head event: {}", event);
            });
            multiClient.addEventListener(EventType.BLOCK, (BeaconEvent event) -> {
                logger.info("Received block event: {}", event);
            });
            multiClient.addEventListener(EventType.FINALIZED_CHECKPOINT, (BeaconEvent event) -> {
                logger.info("Received finalized checkpoint event: {}", event);
            });

            // For other event types, you would add more listeners here

            // Start the client
            multiClient.start();

            // Wait for some events
            Thread.sleep(300000);
        }

        // Example 3: Using the builder directly for more control
        CountDownLatch latch = new CountDownLatch(1);

        BeaconEventClient customClient = new dev.amilcar.eth.events.client.BeaconEventClientBuilder()
                .nodeUrl(nodeUrl)
                .addEventType(EventType.HEAD)
                .build();

        customClient.addEventListener(EventType.HEAD, (BeaconEvent event) -> {
            logger.info("Received head event: {}", event);
            latch.countDown();
        });

        customClient.start();

        // Wait for one event or timeout after 60 seconds
        boolean received = latch.await(60, TimeUnit.SECONDS);

        if (received) {
            logger.info("Successfully received an event!");
        } else {
            logger.warn("Timed out waiting for an event");
        }

        customClient.close();
    }
}
