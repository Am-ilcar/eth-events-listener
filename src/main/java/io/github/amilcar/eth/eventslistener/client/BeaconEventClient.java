package io.github.amilcar.eth.eventslistener.client;

import io.github.amilcar.eth.eventslistener.listener.BeaconEventListener;
import io.github.amilcar.eth.eventslistener.model.BeaconEvent;
import io.github.amilcar.eth.eventslistener.model.EventType;

import java.io.Closeable;
import java.util.Set;

/**
 * Client for subscribing to Ethereum Beacon Chain eventslistener.
 * This interface defines the main API for the library.
 */
public interface BeaconEventClient extends Closeable {
    
    /**
     * Subscribes to eventslistener of the specified types.
     * 
     * @param eventTypes the types of eventslistener to subscribe to
     * @return this client instance for method chaining
     */
    BeaconEventClient subscribe(Set<EventType> eventTypes);
    
    /**
     * Subscribes to eventslistener of the specified type.
     * 
     * @param eventType the type of eventslistener to subscribe to
     * @return this client instance for method chaining
     */
    default BeaconEventClient subscribe(EventType eventType) {
        return subscribe(Set.of(eventType));
    }
    
    /**
     * Adds a listener for eventslistener of the specified type.
     * 
     * @param eventType the type of eventslistener to listen for
     * @param listener the listener to add
     * @param <T> the type of event
     * @return this client instance for method chaining
     */
    <T extends BeaconEvent> BeaconEventClient addEventListener(EventType eventType, BeaconEventListener<T> listener);
    
    /**
     * Removes a listener for eventslistener of the specified type.
     * 
     * @param eventType the type of eventslistener to stop listening for
     * @param listener the listener to remove
     * @param <T> the type of event
     * @return this client instance for method chaining
     */
    <T extends BeaconEvent> BeaconEventClient removeEventListener(EventType eventType, BeaconEventListener<T> listener);
    
    /**
     * Starts listening for eventslistener.
     * This method establishes the connection to the Ethereum node and begins receiving eventslistener.
     * 
     * @return this client instance for method chaining
     */
    BeaconEventClient start();
    
    /**
     * Stops listening for eventslistener.
     * This method closes the connection to the Ethereum node and stops receiving eventslistener.
     * 
     * @return this client instance for method chaining
     */
    BeaconEventClient stop();
    
    /**
     * Checks if the client is currently connected and listening for eventslistener.
     * 
     * @return true if the client is connected, false otherwise
     */
    boolean isConnected();
    
    /**
     * Gets the URL of the Ethereum node that this client is connected to.
     * 
     * @return the URL of the Ethereum node
     */
    String getNodeUrl();
}